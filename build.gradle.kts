plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("plugin.jpa") version "1.9.20"
    kotlin("kapt") version "1.9.20"
}

group = "com.manticore"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    /**
     * Spring
     */
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    /**
     * DB
     */
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    kapt("org.hibernate.orm:hibernate-jpamodelgen:6.2.5.Final")
    implementation("org.hibernate.orm:hibernate-jpamodelgen:6.2.5.Final")

    /**
     * Minio
     */
    implementation("io.minio:minio:8.5.7")

    /**
     * Test
     */
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:minio:1.19.5")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("io.rest-assured:rest-assured:5.3.2")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    /**
     * Swagger
     */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    /**
     * Utils & Logging
     */
    testImplementation(kotlin("test"))
    implementation("org.mapstruct:mapstruct:1.5.3.Final")
    kapt("org.mapstruct:mapstruct-processor:1.5.3.Final")
    implementation("io.github.oshai:kotlin-logging:5.1.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
    }
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

tasks.named("compileKotlin") {
    dependsOn("kaptKotlin")
}