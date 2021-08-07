package com.johngachihi.parking

import java.time.Instant
import java.time.temporal.ChronoUnit

val Int.minutesAgo: Instant
    get() = Instant.now().minus(this.toLong(), ChronoUnit.MINUTES)