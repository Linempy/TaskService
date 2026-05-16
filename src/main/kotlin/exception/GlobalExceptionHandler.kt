package com.manticore.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.Instant
import java.util.NoSuchElementException

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val timestamp: Instant = Instant.now(),
        val status: Int,
        val error: String,
        val message: String?,
        val path: String?
    )

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.warn { "Resource not found: ${ex.message}" }
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.message, request)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.warn { "Illegal state: ${ex.message}" }
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.message, request)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.warn { "Bad request: ${ex.message}" }
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.message, request)
    }

    @ExceptionHandler(StorageException::class)
    fun handleStorageError(ex: StorageException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error(ex) { "Storage operation failed: ${ex.message}" }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Storage Error", ex.message, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericError(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error(ex) { "Unexpected error occurred" }
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn { "Validation failed: ${ex.message}" }

        val errors = ex.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            errors,
            request
        )
    }

    private fun buildResponse(
        status: HttpStatus,
        error: String,
        message: String?,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val path = request.getDescription(false)
            .substringAfter("uri=", "")
        val body = ErrorResponse(
            timestamp = Instant.now(),
            status = status.value(),
            error = error,
            message = message,
            path = path
        )
        return ResponseEntity.status(status).body(body)
    }
}