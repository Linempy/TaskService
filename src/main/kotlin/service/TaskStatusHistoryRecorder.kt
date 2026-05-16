package com.manticore.service

import com.fasterxml.jackson.databind.JsonNode
import com.manticore.model.TaskStatus
import com.manticore.model.TaskStatusHistory
import com.manticore.repository.TaskStatusHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TaskStatusHistoryRecorder(
    private val repository: TaskStatusHistoryRepository,
) {

    @Transactional(propagation = Propagation.MANDATORY)
    fun recordTransition(
        taskId: Long,
        oldStatus: TaskStatus?,
        newStatus: TaskStatus,
        changedBy: String,
        metadata: JsonNode? = null,
    ) {
        if (oldStatus == newStatus) return
        repository.save(
            TaskStatusHistory(
                taskId = taskId,
                oldStatus = oldStatus,
                newStatus = newStatus,
                changedBy = changedBy,
                metadata = metadata,
            ),
        )
    }
}
