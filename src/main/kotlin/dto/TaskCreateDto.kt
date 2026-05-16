package com.manticore.dto

import com.manticore.model.TaskType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TaskCreateDto(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "FileKey is required")
    val fileKey: String,

    @field:Min(value = 0, message = "Priority must be at least 0")
    @field:Max(value = 10, message = "Priority cannot exceed 10")
    val priority: Int? = 0,

    @field:NotNull(message = "TaskType is required")
    val taskType: TaskType
)
