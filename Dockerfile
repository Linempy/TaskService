FROM gradle:7.6.6-jdk17 AS builder

WORKDIR /workspace

COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY src src

RUN chmod +x gradlew

RUN ./gradlew clean bootJar -x test --no-daemon

RUN ls -la /workspace/build/libs/

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]