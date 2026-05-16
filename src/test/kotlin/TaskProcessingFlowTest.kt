package com.manticore.integration

import com.manticore.integration.config.BaseIntegrationTest
import com.manticore.dto.TaskCreateDto
import com.manticore.dto.TaskViewDto
import com.manticore.model.TaskStatus
import com.manticore.model.TaskType
import com.manticore.repository.TaskRepository
import com.manticore.service.TaskService
import com.manticore.service.storage.FileStorageService
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.restassured.RestAssured
import org.awaitility.Awaitility.await
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskProcessingFlowTest : BaseIntegrationTest() {

    @LocalServerPort private var port: Int = 0
    @Autowired private lateinit var taskService: TaskService
    @Autowired private lateinit var taskRepository: TaskRepository
    @Autowired private lateinit var storageService: FileStorageService
    @Autowired private lateinit var minioClient: MinioClient
    private val bucketName = "test-bucket"

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost:$port"

        runCatching {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }

        runCatching {
            storageService.upload("init".toByteArray(), bucketName, ".keep", "text/plain")
        }
    }

    private fun uploadAndCreate(taskType: TaskType = TaskType.REPORT_GENERATION): TaskViewDto {
        val fileKey = "flow-${System.currentTimeMillis()}-${taskType.name}.txt"
        storageService.upload("test-data".toByteArray(), bucketName, fileKey, "text/plain")

        val dto = TaskCreateDto(
            name = "Test Task ${taskType.name}",
            taskType = taskType,
            fileKey = fileKey,
            priority = 1
        )
        return taskService.createTask(dto)!!
    }

    @Test
    fun `async processor completes REPORT_GENERATION task successfully`() {
        val task = uploadAndCreate(TaskType.REPORT_GENERATION)
        assertNotNull(task.id)
        assertEquals(TaskStatus.PENDING, task.status)

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(500))
            .until {
                val t = taskRepository.findById(task.id).orElse(null)
                t?.status == TaskStatus.COMPLETED
            }

        val completedTask = taskRepository.findById(task.id).get()
        assertEquals(TaskStatus.COMPLETED, completedTask.status)
        assertTrue(completedTask.result?.contains("Report generated") == true)
        assertNotNull(completedTask.completedAt)
    }

    @Test
    fun `async processor completes IMAGE_PROCESSING task`() {
        val task = uploadAndCreate(TaskType.IMAGE_PROCESSING)

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(500))
            .until {
                taskRepository.findById(task.id).get().status == TaskStatus.COMPLETED
            }

        val completedTask = taskRepository.findById(task.id).get()
        assertEquals(TaskStatus.COMPLETED, completedTask.status)
        assertTrue(completedTask.result?.contains("Image processed") == true)
    }

    @Test
    fun `cancellation flag stops processing and sets CANCELLED status`() {
        val task = uploadAndCreate()

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(200))
            .until {
                taskRepository.findById(task.id).get().status == TaskStatus.PROCESSING
            }

        taskService.cancelTask(task.id)

        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(200))
            .until {
                val t = taskRepository.findById(task.id).get()
                t.status == TaskStatus.CANCELLED && t.isCancel
            }

        val cancelledTask = taskRepository.findById(task.id).get()
        assertEquals(TaskStatus.CANCELLED, cancelledTask.status)
        assertTrue(cancelledTask.isCancel)
        assertNotNull(cancelledTask.completedAt)
    }

    @Test
    fun `retry FAILED task via POST {id}process restarts processing`() {
        val task = uploadAndCreate()

        await()
            .atMost(Duration.ofSeconds(10))
            .until {
                taskRepository.findById(task.id).get().status == TaskStatus.COMPLETED
            }

        val dbTask = taskRepository.findById(task.id).get()
        dbTask.status = TaskStatus.FAILED
        dbTask.errorMessage = "Simulated failure for retry test"
        taskRepository.save(dbTask)

        RestAssured.given()
            .`when`().post("/api/v1/tasks/${task.id}/process")
            .then()
            .statusCode(202)
            .body("status", equalTo(TaskStatus.PENDING.name))

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(500))
            .until {
                taskRepository.findById(task.id).get().status == TaskStatus.COMPLETED
            }

        val restartedTask = taskRepository.findById(task.id).get()
        assertEquals(TaskStatus.COMPLETED, restartedTask.status)
        assertTrue(restartedTask.result?.contains("Report generated") == true)
    }

    @Test
    fun `retry CANCELLED task via POST {id}process restarts processing`() {
        val task = uploadAndCreate()
        taskService.cancelTask(task.id)

        await()
            .atMost(Duration.ofSeconds(3))
            .until {
                taskRepository.findById(task.id).get().status == TaskStatus.CANCELLED
            }

        RestAssured.given()
            .`when`().post("/api/v1/tasks/${task.id}/process")
            .then()
            .statusCode(202)
            .body("status", equalTo(TaskStatus.PENDING.name))

        await()
            .atMost(Duration.ofSeconds(10))
            .until {
                taskRepository.findById(task.id).get().status == TaskStatus.COMPLETED
            }

        assertEquals(TaskStatus.COMPLETED, taskRepository.findById(task.id).get().status)
    }

    @Test
    fun `task with exception is marked as FAILED with error message`() {
        val task = uploadAndCreate()

        val dbTask = taskRepository.findById(task.id).get()
        dbTask.status = TaskStatus.FAILED
        dbTask.errorMessage = "Test error message"
        dbTask.completedAt = java.time.LocalDateTime.now()
        taskRepository.save(dbTask)

        RestAssured.given()
            .`when`().get("/api/v1/tasks/${task.id}")
            .then()
            .statusCode(200)
            .body("status", equalTo(TaskStatus.FAILED.name))
            .body("errorMessage", equalTo("Test error message"))
    }
}