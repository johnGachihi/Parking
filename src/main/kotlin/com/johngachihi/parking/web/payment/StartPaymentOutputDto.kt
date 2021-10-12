package com.johngachihi.parking.web.payment

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.Instant

class StartPaymentOutputDto(
    paymentSessionId: Long,
    paymentSessionExpiryTime: Instant,
    paymentAmount: Double,
    ongoingVisitTimeOfStay: Duration
) {
    @JsonProperty("paymentSession")
    val paymentSessionDto = PaymentSessionDto(
        paymentSessionId,
        paymentAmount,
        paymentSessionExpiryTime
    )
    
    val visitTimeOfStay = ongoingVisitTimeOfStay
    
    inner class PaymentSessionDto(val id: Long, val paymentAmount: Double, val expiryTime: Instant)
}
