package com.johngachihi.parking.entities.payment

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Payment.isExpired(maxAgeBeforePaymentExpiry: Duration): Boolean {
    val age = this.madeAt.until(Instant.now(), ChronoUnit.MINUTES)

    return age > maxAgeBeforePaymentExpiry.toMinutes()
}