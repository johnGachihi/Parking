package com.johngachihi.parking.repositories.config

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.IllegalStateException
import java.time.Duration

@DisplayName("Test DefaultParkingFeeConfigRepositoryTest")
@ExtendWith(MockKExtension::class)
internal class DefaultParkingFeeConfigRepositoryTest {
    @MockK
    private lateinit var configRepositoryHelper: ConfigRepositoryHelper

    @InjectMockKs
    private lateinit var parkingFeeConfigRepository: DefaultParkingFeeConfigRepository

    @Nested
    @DisplayName("Test maxAgeBeforePaymentExpiry")
    inner class TestMaxAgeBeforePaymentExpiration {
        @Test
        fun `When not set, then returns default`() {
            every {
                configRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
            } returns null

            assertThat(parkingFeeConfigRepository.maxAgeBeforePaymentExpiry)
                .isEqualTo(Duration.ofMinutes(20)) // Default. TODO: should it be here
        }

        @Test
        fun `When set with a valid value (integer), then returns its value`() {
            every {
                configRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
            } returns "20"

            assertThat(parkingFeeConfigRepository.maxAgeBeforePaymentExpiry)
                .isEqualTo(Duration.ofMinutes(20))
        }

        @Test
        fun `When set with an invalid value (not integer), then throws IllegalStateException`() {
            every {
                configRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
            } returns "a string"

            assertThatExceptionOfType(IllegalStateException::class.java)
                .isThrownBy {
                    parkingFeeConfigRepository.maxAgeBeforePaymentExpiry
                }
                .withMessage("Invalid `Max Age Before Payment Expiry (In Minutes)` setting (a string). Not a number.")
        }
    }
}