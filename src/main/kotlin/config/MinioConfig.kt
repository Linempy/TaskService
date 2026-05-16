package com.manticore.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Конфигурационный класс для настройки клиента MinIO.
 *
 * Создает и настраивает бины для работы с объектным хранилищем MinIO.
 * Параметры подключения берутся из конфигурации приложения
 *
 * @author Linempy
 * @since 15.05.2026
 */
@Configuration
class MinioConfig {
    @Value("\${services.s3.endpoint}")
    private val endpoint: String? = null

    @Value("\${services.s3.access-key}")
    private val accessKey: String? = null

    @Value("\${services.s3.secret-key}")
    private val secretKey: String? = null

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }
}