package com.manticore.service

import com.manticore.model.TaskType
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class TaskProcessingMetrics(
    private val meterRegistry: MeterRegistry,
) {

    enum class Outcome {
        SUCCESS,
        FAILED,
        CANCELLED,
    }

    fun recordProcessingDuration(taskType: TaskType?, outcome: Outcome, durationMs: Long) {
        val typeTag = taskType?.name ?: "UNKNOWN"
        Timer.builder("tasks.processing.duration")
            .description("Время обработки задачи от перевода в PROCESSING до терминального статуса")
            .tag("task_type", typeTag)
            .tag("outcome", outcome.name.lowercase())
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS)
    }
}
