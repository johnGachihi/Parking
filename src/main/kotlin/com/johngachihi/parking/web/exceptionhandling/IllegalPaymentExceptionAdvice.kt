package com.johngachihi.parking.web.exceptionhandling

import com.johngachihi.parking.services.payment.IllegalPaymentException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class IllegalPaymentExceptionAdvice {
    @ExceptionHandler(IllegalPaymentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleException(e: IllegalPaymentException): IllegalPaymentErrorResponse {
        return IllegalPaymentErrorResponse(e.message)
    }
}

data class IllegalPaymentErrorResponse(override val detail: String) : ErrorResponse() {
    override val type: String = "illegal-payment-attempt"
    override val title: String = "Illegal payment attempt"
    override val status: HttpStatus = HttpStatus.BAD_REQUEST
}
