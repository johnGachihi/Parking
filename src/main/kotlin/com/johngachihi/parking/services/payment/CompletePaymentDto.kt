package com.johngachihi.parking.services.payment

import javax.validation.constraints.Positive

data class CompletePaymentDto(
    @get:Positive(message = "A payment-session ID is required and must be greater than zero")
    val paymentSessionId: Long
)