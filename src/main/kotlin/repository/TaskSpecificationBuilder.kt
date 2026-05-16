package com.manticore.repository

import com.manticore.dto.TaskFilterDto
import com.manticore.model.Task
import com.manticore.model.TaskStatus
import com.manticore.model.TaskType
import com.manticore.model.Task_
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

/**
 * Builder спецификаций для фильтрации сущностей Task.
 *
 * @author Linempy
 * @since 15.05.2026
 */
class TaskSpecificationBuilder {

    companion object {

        fun buildSpecification(filter: TaskFilterDto): Specification<Task> {
            return Specification.allOf(
                byStatus(filter.status),
                byTaskType(filter.taskType),
                byCreatedFrom(filter.createdFrom),
                byCreatedTo(filter.createdTo)
            )
        }

        fun byStatus(status: TaskStatus?): Specification<Task>? {
            if (status == null) return null

            return Specification { root, _, cb ->
                cb.equal(root.get(Task_.status), status)
            }
        }

        fun byTaskType(taskType: TaskType?): Specification<Task>? {
            if (taskType == null) return null

            return Specification { root, _, cb ->
                cb.equal(root.get(Task_.taskType), taskType)
            }
        }

        fun byCreatedFrom(date: LocalDateTime?): Specification<Task>? {
            if (date == null) return null

            return Specification { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get(Task_.createdAt), date)
            }
        }

        fun byCreatedTo(date: LocalDateTime?): Specification<Task>? {
            if (date == null) return null

            return Specification { root, _, cb ->
                cb.lessThanOrEqualTo(root.get(Task_.createdAt), date)
            }
        }
    }
}