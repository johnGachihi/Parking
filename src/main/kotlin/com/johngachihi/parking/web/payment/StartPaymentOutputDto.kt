package com.johngachihi.parking.web.payment

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.Instant

class StartPaymentOutputDto(
    paymentSessionId: Long,
    paymentSessionExpiryTime: Instant,
    paymentAmount: Double,
    ongoingVisitTimeOfStay: Duration,
    ongoingVisitEntryTime: Instant,
    ongoingVisitLatestPaymentTime: Instant?
) {
    @JsonProperty("paymentSession")
    val paymentSessionDto = PaymentSessionDto(
        paymentSessionId,
        paymentAmount,
        paymentSessionExpiryTime
    )

    val visit = VisitDto(ongoingVisitTimeOfStay, ongoingVisitEntryTime, ongoingVisitLatestPaymentTime)

    inner class PaymentSessionDto(val id: Long, val paymentAmount: Double, val expiryTime: Instant)
    inner class VisitDto(val timeOfStay: Duration, val entryTime: Instant, val latestPaymentTime: Instant?)
}
