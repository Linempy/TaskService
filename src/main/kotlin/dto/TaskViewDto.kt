package com.manticore.dto

import com.manticore.model.TaskStatus
import com.manticore.model.TaskType
import java.time.LocalDateTime

data class TaskViewDto(
    var id: Long,
    var name: String,
    var status: TaskStatus,
    var taskType: TaskType,
    var priority: Int,
    var errorMessage: String? = null,
    var result: String? = null,
    var isCancel: Boolean = false,
    var createdAt: LocalDateTime
)