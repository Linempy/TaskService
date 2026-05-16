package com.manticore.mapper

import com.manticore.dto.TaskCreateDto
import com.manticore.dto.TaskViewDto
import com.manticore.model.Task
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface TaskMapper {

    fun toDto(task: Task): TaskViewDto

    fun toEntity(taskViewDto: TaskCreateDto): Task
}