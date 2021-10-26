package com.johngachihi.parking.web.payment

import javax.validation.constraints.Positive

data class CancelPaymentDto(
    @get:Positive(message = "A payment-session ID is required and must be greater than zero")
    val paymentSessionId: Long
)
