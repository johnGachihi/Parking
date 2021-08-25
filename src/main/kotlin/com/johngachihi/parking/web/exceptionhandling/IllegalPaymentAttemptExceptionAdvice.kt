package com.johngachihi.parking.web.exceptionhandling

import com.johngachihi.parking.services.payment.IllegalPaymentAttemptException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class IllegalPaymentAttemptExceptionAdvice {
    @ExceptionHandler(IllegalPaymentAttemptException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleException(e: IllegalPaymentAttemptException): IllegalPaymentAttemptErrorResponse {
        return IllegalPaymentAttemptErrorResponse(e.message)
    }
}

data class IllegalPaymentAttemptErrorResponse(override val detail: String) : ErrorResponse() {
    override val type: String = "illegal-payment-attempt"
    override val title: String = "Illegal payment attempt"
    override val status: HttpStatus = HttpStatus.BAD_REQUEST
}
