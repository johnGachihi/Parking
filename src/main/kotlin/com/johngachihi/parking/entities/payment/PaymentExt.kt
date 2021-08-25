package com.johngachihi.parking.entities.payment

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Payment.isExpired(maxAgeBeforePaymentExpiry: Duration): Boolean {
    val age = this.finishedAt.until(Instant.now(), ChronoUnit.MINUTES)

    return age > maxAgeBeforePaymentExpiry.toMinutes()
}