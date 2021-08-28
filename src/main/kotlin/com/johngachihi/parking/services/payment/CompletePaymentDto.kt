package com.johngachihi.parking.services.payment

import javax.validation.constraints.Positive

data class CompletePaymentDto(
    val paymentSessionId: Long
)