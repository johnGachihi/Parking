package com.johngachihi.parking.web.exceptionhandling

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class ValidationErrorAdvice {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationError(e: MethodArgumentNotValidException): ValidationErrorResponse {
        val violations = e.fieldErrors.groupBy(
            keySelector = { it.field },
            valueTransform = { it.defaultMessage }
        )
        return ValidationErrorResponse(violations)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationError(e: ConstraintViolationException): ValidationErrorResponse {
        val violations = e.constraintViolations.groupBy(
            keySelector = { it.propertyPath.toString() },
            valueTransform = { it.message }
        )

        return ValidationErrorResponse(violations)
    }
}


data class ValidationErrorResponse(
    val violations: Map<String, List<String?>>,
    override val detail: String = "The inputs for the following fields are invalid: ${violations.keys}",
) : ErrorResponse() {
    override val type: String = "validation-error"
    override val status: HttpStatus = HttpStatus.BAD_REQUEST
    override val title: String = "Validation error"
}