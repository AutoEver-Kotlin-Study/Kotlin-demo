package com.example.haeautoeverstudy.application.adapter.`in`.web

import com.example.haeautoeverstudy.application.domain.model.exception.DomainModelException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupAlreadyExistsException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupCapacityExceededException
import com.example.haeautoeverstudy.application.domain.model.exception.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(GroupCapacityExceededException::class)
    fun handleGroupCapacityExceeded(exception: GroupCapacityExceededException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(message = exception.message.orEmpty()))

    @ExceptionHandler(GroupAlreadyExistsException::class)
    fun handleGroupAlreadyExists(exception: GroupAlreadyExistsException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(message = exception.message.orEmpty()))

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(exception: UserAlreadyExistsException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(message = exception.message.orEmpty()))

    @ExceptionHandler(DomainModelException::class)
    fun handleDomainModelException(exception: DomainModelException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(message = exception.message.orEmpty()))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(message = exception.message.orEmpty()))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(exception: NoSuchElementException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(message = exception.message.orEmpty()))
}

data class ApiErrorResponse(
    val message: String,
)
