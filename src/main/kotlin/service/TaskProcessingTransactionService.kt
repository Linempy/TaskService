package com.manticore.service

import com.manticore.model.Task
import com.manticore.model.TaskStatus
import com.manticore.model.TaskType
import com.manticore.repository.TaskRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

data class TaskSnapshot(val taskType: TaskType, val fileKey: String)

/**
 * Короткие транзакции для обработки задач
 */
@Service
class TaskProcessingTransactionService(
    private val taskRepository: TaskRepository,
    private val historyRecorder: TaskStatusHistoryRecorder,
) {

    @Transactional
    fun claimTask(taskId: Long): Boolean {
        val updated = taskRepository.updateStatusIfPendingToProcessing(
            taskId,
            TaskStatus.PROCESSING,
            TaskStatus.PENDING,
        )
        if (updated > 0) {
            historyRecorder.recordTransition(
                taskId,
                TaskStatus.PENDING,
                TaskStatus.PROCESSING,
                "WORKER",
            )
        }
        return updated > 0
    }

    @Transactional(readOnly = true)
    fun loadTaskSnapshot(taskId: Long): TaskSnapshot? {
        val task = taskRepository.findById(taskId).orElse(null) ?: return null
        return TaskSnapshot(task.taskType, task.fileKey)
    }

    @Transactional
    fun completeSuccess(taskId: Long, result: String) {
        saveSafely(taskId) { t ->
            if (t.status == TaskStatus.CANCELLED || t.isCancel) {
                return@saveSafely
            }
            t.status = TaskStatus.COMPLETED
            t.result = result
            t.completedAt = LocalDateTime.now()
        }
    }

    @Transactional
    fun ensureCancelled(taskId: Long) {
        saveSafely(taskId) { t ->
            if (t.status == TaskStatus.CANCELLED) {
                return@saveSafely
            }
            t.status = TaskStatus.CANCELLED
            t.completedAt = LocalDateTime.now()
        }
    }

    @Transactional
    fun completeFailure(taskId: Long, message: String) {
        saveSafely(taskId) { t ->
            if (t.status == TaskStatus.CANCELLED || t.isCancel) {
                return@saveSafely
            }
            t.status = TaskStatus.FAILED
            t.errorMessage = message
            t.completedAt = LocalDateTime.now()
        }
    }

    private fun saveSafely(taskId: Long, block: (Task) -> Unit) {
        try {
            val t = taskRepository.findById(taskId).orElseThrow()
            val oldStatus = t.status
            block(t)
            val newStatus = t.status
            taskRepository.save(t)
            if (oldStatus != null && newStatus != null && oldStatus != newStatus) {
                historyRecorder.recordTransition(taskId, oldStatus, newStatus, "WORKER")
            }
        } catch (e: ObjectOptimisticLockingFailureException) {
            log.debug(e) { "Concurrent update for task $taskId, skipping save" }
        }
    }
}
