package com.manticore.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime


@Entity
@Table(name = "tasks")
class Task(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TaskStatus?,

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    var taskType: TaskType,

    @Column(name = "file_key", nullable = false)
    var fileKey: String,

    @Column(name = "priority", nullable = false)
    var priority: Int = 0,

    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: LocalDateTime?,

    @Column(name = "result", columnDefinition = "TEXT")
    var result: String? = null,

    @Column(name = "error_message", columnDefinition = "TEXT")
    var errorMessage: String? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null,

    @Column(name = "is_cancel", nullable = false)
    var isCancel: Boolean = false,

    @Version
    var version: Long = 0

)
