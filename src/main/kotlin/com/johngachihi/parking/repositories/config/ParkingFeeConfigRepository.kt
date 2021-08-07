package com.johngachihi.parking.repositories.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.Duration

interface ParkingFeeConfigRepository {
    val maxAgeBeforePaymentExpiry: Duration
}

@Repository
class DefaultParkingFeeConfigRepository(
    @Autowired private val configRepositoryHelper: ConfigRepositoryHelper
) : ParkingFeeConfigRepository {
    override val maxAgeBeforePaymentExpiry: Duration
        get() {
            val value = configRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
                ?: return Duration.ofMinutes(20) // Default value

            try {
                return Duration.ofMinutes(value.toLong())
            } catch (e: NumberFormatException) {
                throw IllegalStateException("Invalid `Max Age Before Payment Expiry (In Minutes)` setting ($value). Not a number.", e)
            }
        }
}

