package com.manticore.integration

import com.manticore.integration.config.BaseIntegrationTest
import com.manticore.dto.TaskCreateDto
import com.manticore.dto.TaskViewDto
import com.manticore.model.TaskStatus
import com.manticore.model.TaskType
import com.manticore.service.storage.FileStorageService
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskApiIntegrationTest : BaseIntegrationTest() {

    @LocalServerPort private var port: Int = 0
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

    private fun uploadTestFile(suffix: String = ""): String {
        val key = "test-${System.currentTimeMillis()}-$suffix.txt"
        storageService.upload("data".toByteArray(), bucketName, key, "text/plain")
        return key
    }

    @Test
    fun `create task returns 201 and PENDING status`() {
        val fileKey = uploadTestFile("create")
        val dto = TaskCreateDto(name = "name1", taskType = TaskType.REPORT_GENERATION, fileKey = fileKey, priority = 1)

        val response = RestAssured.given()
            .contentType(ContentType.JSON).body(dto)
            .`when`().post("/api/v1/tasks")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("status", equalTo(TaskStatus.PENDING.name))
            .body("id", notNullValue())
            .extract()
            .`as`(TaskViewDto::class.java)

        assertNotNull(response.id)
        assertEquals(TaskStatus.PENDING, response.status)
    }

    @Test
    fun `create task fails when file missing`() {
        val dto = TaskCreateDto(name = "name2", taskType = TaskType.DATA_EXPORT, fileKey = "nonexistent.txt", priority = 1)

        RestAssured.given()
            .contentType(ContentType.JSON).body(dto)
            .`when`().post("/api/v1/tasks")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("error", equalTo("Bad Request"))
    }

    @Test
    fun `list tasks with pagination`() {
        repeat(3) { i ->
            val fk = uploadTestFile("list-$i")
            RestAssured.given().contentType(ContentType.JSON)
                .body(TaskCreateDto(name = "name3", taskType = TaskType.REPORT_GENERATION, fileKey = fk, priority = 1))
                .`when`().post("/api/v1/tasks")
        }

        RestAssured.given()
            .`when`().get("/api/v1/tasks?page=0&size=2")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
            .body("totalElements", equalTo(3))
            .body("totalPages", equalTo(2))
    }

    @Test
    fun `cancel pending task`() {
        val fk = uploadTestFile("cancel-pending")
        val created = RestAssured.given().contentType(ContentType.JSON)
            .body(TaskCreateDto(name = "name4", taskType = TaskType.REPORT_GENERATION, fileKey = fk, priority = 1))
            .`when`().post("/api/v1/tasks")
            .then().extract().`as`(TaskViewDto::class.java)

        RestAssured.given()
            .`when`().delete("/api/v1/tasks/${created.id}")
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
            .body("status", equalTo(TaskStatus.CANCELLED.name))
    }

    @Test
    fun `retry cancelled task via POST process`() {
        val fk = uploadTestFile("retry")
        val created = RestAssured.given().contentType(ContentType.JSON)
            .body(TaskCreateDto(name = "name5", taskType = TaskType.REPORT_GENERATION, fileKey = fk, priority = 1))
            .`when`().post("/api/v1/tasks")
            .then().extract().`as`(TaskViewDto::class.java)

        RestAssured.given().`when`().delete("/api/v1/tasks/${created.id}")

        RestAssured.given()
            .`when`().post("/api/v1/tasks/${created.id}/process")
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
            .body("status", equalTo(TaskStatus.PENDING.name))
    }
}