package com.manticore.integration.config

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class BaseIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgresqlContainer = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")

        @Container
        @JvmStatic
        val minioContainer = MinIOContainer(DockerImageName.parse("minio/minio:latest"))
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresqlContainer::getUsername)
            registry.add("spring.datasource.password", postgresqlContainer::getPassword)

            registry.add("services.s3.endpoint") {
                "http://${minioContainer.host}:${minioContainer.firstMappedPort}"
            }
            registry.add("services.s3.access-key") { minioContainer.userName }
            registry.add("services.s3.secret-key") { minioContainer.password }
            registry.add("services.s3.bucketName") { "test-bucket" }

            registry.add("app.task.poll-interval") { "500" }
            registry.add("thread-pool.async.task.max-pool-size") { "2" }

            registry.add("spring.liquibase.enabled") { true }
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }

            registry.add("spring.datasource.hikari.maximum-pool-size") { "20" }
            registry.add("spring.datasource.hikari.connection-timeout") { "60000" }
        }
    }
}