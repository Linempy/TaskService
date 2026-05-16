package com.manticore.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean("asyncTaskExecutor")
    fun getAsyncExecutor(
        @Value("\${thread-pool.async.task.core-pool-size}") corePoolSize: Int,
        @Value("\${thread-pool.async.task.max-pool-size}") maxPoolSize: Int,
        @Value("\${thread-pool.async.task.queue-capacity}") queueCapacity: Int
    ): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.queueCapacity = queueCapacity
        executor.maxPoolSize = maxPoolSize
        executor.corePoolSize = corePoolSize

        executor.threadNamePrefix = "Async-TaskWorker-"
        executor.initialize()
        return executor
    }
}