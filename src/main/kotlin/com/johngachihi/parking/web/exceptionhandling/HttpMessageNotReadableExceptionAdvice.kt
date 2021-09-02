package com.johngachihi.parking.web.exceptionhandling

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class HttpMessageNotReadableExceptionAdvice {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // TODO: Handle case when JsonParseException is thrown
    fun handleException(e: HttpMessageNotReadableException): ErrorResponse {
        if (e.cause is MismatchedInputException)
            return InvalidInputDataFormatErrorResponse()
        else
            throw e
    }
}

class InvalidInputDataFormatErrorResponse: ErrorResponse() {
    override val type: String = "invalid-input-data-format"
    override val title: String = "Invalid input data format"
    override val status: HttpStatus = HttpStatus.BAD_REQUEST
    override val detail: String? = null
}