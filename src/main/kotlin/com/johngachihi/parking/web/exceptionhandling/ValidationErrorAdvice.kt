package com.johngachihi.parking.web.exceptionhandling

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ValidationErrorAdvice {
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun a(e: MethodArgumentNotValidException): ValidationErrorResponse {
        val violations = e.fieldErrors.groupBy(
            keySelector = { it.field },
            valueTransform = { it.defaultMessage }
        )
        return ValidationErrorResponse(violations)
    }
}