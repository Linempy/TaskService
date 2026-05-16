package com.manticore.dto

import com.fasterxml.jackson.databind.JsonNode
import com.manticore.model.TaskStatus
import java.time.Instant

data class TaskStatusHistoryViewDto(
    val id: Long,
    val taskId: Long,
    val oldStatus: TaskStatus?,
    val newStatus: TaskStatus,
    val changedAt: Instant,
    val changedBy: String,
    val metadata: JsonNode?,
)
