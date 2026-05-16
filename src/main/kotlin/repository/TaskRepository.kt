package com.manticore.repository

import com.manticore.model.Task
import com.manticore.model.TaskStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface TaskRepository : JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    fun countByStatus(status: TaskStatus): Long

    @Query("""
        SELECT t.isCancel FROM Task t 
        WHERE t.id = :id
        """)
    fun isCancellationTask(@Param("id") id: Long): Boolean

    @Query("""
        SELECT t FROM Task t 
        WHERE t.status = :status 
        ORDER BY t.priority DESC, t.createdAt ASC 
    """)
    fun findTopNByStatusOrderByPriorityDescCreatedAtAsc(
        @Param("status") status: TaskStatus,
        pageable: Pageable
    ): List<Task>

    @Modifying
    @Query("""
        UPDATE Task t 
        SET t.status = :newStatus 
        WHERE t.id = :id AND t.status = :currentStatus
        """)
    fun updateStatusIfPendingToProcessing(
        @Param("id") id: Long,
        @Param("newStatus") newStatus: TaskStatus = TaskStatus.PROCESSING,
        @Param("currentStatus") currentStatus: TaskStatus = TaskStatus.PENDING
    ): Int

    @Query("""
        SELECT t FROM Task t 
        WHERE t.status = 'PROCESSING' AND t.updatedAt < :threshold
        """)
    fun findStuckTasks(@Param("threshold") threshold: Instant): List<Task>
}