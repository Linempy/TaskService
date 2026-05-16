package com.manticore.repository

import com.manticore.model.TaskStatusHistory
import org.springframework.data.jpa.repository.JpaRepository

interface TaskStatusHistoryRepository : JpaRepository<TaskStatusHistory, Long> {
    fun findByTaskIdOrderByChangedAtAsc(id: Long): List<TaskStatusHistory>
}
