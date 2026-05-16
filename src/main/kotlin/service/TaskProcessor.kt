package com.manticore.service

import com.manticore.exception.TaskCancelledException
import com.manticore.model.TaskType
import com.manticore.repository.TaskRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component


private val log = KotlinLogging.logger {}

/**
 * Асинхронный процессор для выполнения задач.
 *
 * Отвечает за выполнение бизнес-логики обработки задач различных типов.
 * Поддерживает отмену задач, обработку ошибок и оптимистичную блокировку.
 *
 * Работает асинхронно через пул потоков, настраиваемый через конфигурацию.
 * @author Linempy
 * @since 15.05.2026
 */
@Component
class TaskProcessor(
    private val taskRepository: TaskRepository,
    private val processingTx: TaskProcessingTransactionService,
    private val processingMetrics: TaskProcessingMetrics,
) {

    @Async("asyncTaskExecutor")
    fun processTask(taskId: Long) {
        if (!processingTx.claimTask(taskId)) {
            return
        }

        val startTime = System.nanoTime()
        var snapshot: TaskSnapshot? = null

        try {
            checkCancellation(taskId)
            snapshot = processingTx.loadTaskSnapshot(taskId)
            if (snapshot == null) {
                val durationMs = durationMs(startTime)
                processingMetrics.recordProcessingDuration(null, TaskProcessingMetrics.Outcome.FAILED, durationMs)
                return
            }

            log.info { "Processing task $taskId of type ${snapshot.taskType}" }
            val result = when (snapshot.taskType) {
                TaskType.IMAGE_PROCESSING -> processImage(snapshot.fileKey)
                TaskType.DATA_EXPORT -> exportData(snapshot.fileKey)
                TaskType.REPORT_GENERATION -> generateReport(snapshot.fileKey)
            }
            checkCancellation(taskId)

            val durationMs = durationMs(startTime)
            log.info { "Task $taskId completed in ${durationMs}ms" }
            processingTx.completeSuccess(taskId, result)
            processingMetrics.recordProcessingDuration(
                snapshot.taskType,
                TaskProcessingMetrics.Outcome.SUCCESS,
                durationMs,
            )
        } catch (e: TaskCancelledException) {
            log.info { "Task $taskId cancelled" }
            processingTx.ensureCancelled(taskId)
            processingMetrics.recordProcessingDuration(
                snapshot?.taskType,
                TaskProcessingMetrics.Outcome.CANCELLED,
                durationMs(startTime),
            )
        } catch (e: Exception) {
            log.error(e) { "Task $taskId failed" }
            processingTx.completeFailure(taskId, e.message ?: "Unknown error")
            processingMetrics.recordProcessingDuration(
                snapshot?.taskType,
                TaskProcessingMetrics.Outcome.FAILED,
                durationMs(startTime),
            )
        }
    }

    private fun durationMs(startNano: Long): Long =
        (System.nanoTime() - startNano) / 1_000_000

    private fun checkCancellation(taskId: Long) {
        if (taskRepository.isCancellationTask(taskId)) {
            throw TaskCancelledException("Cancellation requested for task $taskId")
        }
    }

    private fun processImage(path: String): String {
        Thread.sleep(2000)
        return "Image processed: $path"
    }

    private fun exportData(path: String): String {
        Thread.sleep(3000)
        return "Data exported: $path"
    }

    private fun generateReport(path: String): String {
        Thread.sleep(1500)
        return "Report generated: $path"
    }
}