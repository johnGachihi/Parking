package com.johngachihi.parking.web.exceptionhandling

import com.johngachihi.parking.InvalidTicketCodeException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class InvalidTicketCodeExceptionAdvice {
    @ExceptionHandler(InvalidTicketCodeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleException(e: InvalidTicketCodeException) = InvalidTicketCodeErrorResponse(e.message)
}

data class InvalidTicketCodeErrorResponse(override val detail: String) : ErrorResponse() {
    override val type: String = "invalid-ticket-code"
    override val title: String = "Invalid ticket code"
    override val status: HttpStatus = HttpStatus.BAD_REQUEST
}
