package com.manticore.model

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "task_status_history")
class TaskStatusHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "task_id", nullable = false)
    val taskId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 63)
    val oldStatus: TaskStatus?,

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 63)
    val newStatus: TaskStatus,

    @Column(name = "changed_at", nullable = false)
    val changedAt: Instant = Instant.now(),

    @Column(name = "changed_by", length = 255)
    val changedBy: String = "SYSTEM",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    val metadata: JsonNode? = null,
)
