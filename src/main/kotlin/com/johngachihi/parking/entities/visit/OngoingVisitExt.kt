package com.johngachihi.parking.entities.visit

import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.payment.isExpired
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

val OngoingVisit.timeOfStay: Duration
    get() {
        val minutesSinceEntry: Long =
            this.entryTime.until(Instant.now(), ChronoUnit.MINUTES)
        return Duration.ofMinutes(minutesSinceEntry)
    }

val OngoingVisit.totalAmountPaid: Double
    get() = this.payments.fold(0.0) { acc, payment -> acc + payment.amount!! }

val OngoingVisit.hasAtLeastOnePayment: Boolean
    get() = this.payments.isNotEmpty()

val OngoingVisit.latestPayment: Payment
    get() = payments.maxByOrNull { it.madeAt }
        ?: throw NoSuchElementException()

val OngoingVisit.latestPaymentOrNull: Payment?
    get() = payments.maxByOrNull { it.madeAt }

// TODO: Move this to Service layer because of its parameter.
//       Or wrap it at point of use (Service layer)
fun OngoingVisit.isInExitAllowancePeriod(
    maxAgeBeforePaymentExpiry: Duration
): Boolean = hasAtLeastOnePayment && !latestPayment.isExpired(maxAgeBeforePaymentExpiry)
