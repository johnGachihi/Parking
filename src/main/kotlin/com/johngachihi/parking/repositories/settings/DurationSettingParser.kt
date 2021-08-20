package com.johngachihi.parking.repositories.settings

import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.Duration

@Component
class DurationSettingParser {
    fun parseMinutesString(
        minutesString: String,
        maxDuration: Duration = Duration.ofDays(10)
    ): Duration {
        val minutes = minutesString.toBigInteger()

        if (minutes.compareTo(BigInteger.ZERO) == -1) {
            throw IllegalFormatException("The value provided is less than 0: $minutes")
        }

        if (minutes.compareTo(maxDuration.toMinutes().toBigInteger()) == 1) {
            throw IllegalFormatException(
                "The value provided ($minutes) is greater than the specified maximum " +
                        "${maxDuration.toMinutes()}"
            )
        }

        return Duration.ofMinutes(minutes.toLong())
    }
}