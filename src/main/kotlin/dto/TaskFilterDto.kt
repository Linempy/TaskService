package com.manticore.dto

import com.manticore.model.TaskStatus
import com.manticore.model.TaskType
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class TaskFilterDto(
    val status: TaskStatus? = null,
    val taskType: TaskType? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val createdFrom: LocalDateTime? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val createdTo: LocalDateTime? = null,
)
