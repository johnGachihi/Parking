package com.johngachihi.parking.web.exceptionhandling

import com.johngachihi.parking.services.payment.IllegalPaymentCancellationAttemptException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class IllegalPaymentCancellationAttemptExceptionAdvice {
    @ExceptionHandler(IllegalPaymentCancellationAttemptException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleException(
        e: IllegalPaymentCancellationAttemptException
    ): IllegalPaymentCancellationAttemptErrorResponse
    {
        return IllegalPaymentCancellationAttemptErrorResponse(e.message)
    }
}

data class IllegalPaymentCancellationAttemptErrorResponse(override val detail: String) : ErrorResponse() {
    override val type = "illegal-payment-cancellation-attempt"
    override val title = "Illegal payment cancellation attempt"
    override val status = HttpStatus.BAD_REQUEST
}
