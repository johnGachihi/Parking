package com.johngachihi.parking.entities.visit

import com.johngachihi.parking.entities.OngoingVisit
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
