package com.manticore.scheduler

import com.manticore.model.TaskStatus
import com.manticore.repository.TaskRepository
import com.manticore.service.TaskProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * Сервис планировщика для управления очередью задач.
 * Периодически проверяет наличие ожидающих задач в БД и отправляет их на асинхронную обработку.
 * Учитывает максимальное количество одновременно выполняемых задач.
 *
 * @author Linempy
 * @since 15.05.2026
 */
@Component
class TaskSchedulerService(
    private val taskRepository: TaskRepository,
    private val taskProcessor: TaskProcessor,
    @Value("\${thread-pool.async.task.max-pool-size:3}") private val maxConcurrent: Long

) {
    @Scheduled(fixedDelayString = "\${app.task.poll-interval:1000}")
    fun pollAndProcess() {
        val processingCount = taskRepository.countByStatus(TaskStatus.PROCESSING)
        val availableSlots = maxConcurrent - processingCount

        if (availableSlots <= 0) {
            log.debug { "No available slots. Processing: $processingCount/$maxConcurrent" }
            return
        }

        val pageable = PageRequest.of(0, availableSlots.toInt())
        val pendingTasks = taskRepository.findTopNByStatusOrderByPriorityDescCreatedAtAsc(
            TaskStatus.PENDING,
            pageable
        )

        pendingTasks.forEach { task ->
            taskProcessor.processTask(task.id!!)
        }
    }
}