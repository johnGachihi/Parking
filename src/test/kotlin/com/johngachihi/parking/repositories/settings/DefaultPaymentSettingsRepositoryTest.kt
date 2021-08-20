package com.johngachihi.parking.repositories.settings

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@DisplayName("Test DefaultPaymentSettingsRepositoryTest")
@ExtendWith(MockKExtension::class)
internal class DefaultPaymentSettingsRepositoryTest {
    @MockK
    private lateinit var settingsRepositoryHelper: SettingsRepositoryHelper

    @MockK
    private lateinit var durationSettingParser: DurationSettingParser

    @InjectMockKs
    private lateinit var parkingFeeSettingsRepository: DefaultPaymentSettingsRepository

    @Nested
    @DisplayName("Test maxAgeBeforePaymentExpiry")
    inner class TestMaxAgeBeforePaymentExpiration {
        @Test
        fun `When not set, then returns default`() {
            every {
                settingsRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
            } returns null

            assertThat(parkingFeeSettingsRepository.maxAgeBeforePaymentExpiry)
                .isEqualTo(Duration.ofMinutes(20)) // Default. TODO: should it be here
        }

        @Test
        fun `When durationSettingParser throws InvalidFormatException, then throws IllegalSettingException`() {
            every {
                settingsRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
            } returns "20"

            every {
                durationSettingParser.parseMinutesString(any())
            } throws IllegalFormatException("cause")

            val throwable = catchThrowable {
                parkingFeeSettingsRepository.maxAgeBeforePaymentExpiry
            }

            assertThat(throwable)
                .isInstanceOf(IllegalSettingException::class.java)
                .hasMessageContaining("Max Age Before Payment Expiry (minutes)")
                .hasCauseExactlyInstanceOf(IllegalFormatException::class.java)
                .hasRootCauseMessage("cause")
        }

        @Test
        fun `When durationSettingParser throws NumberFormatException, then throws IllegalSettingException`() {
            every {
                settingsRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
            } returns "20"

            every {
                durationSettingParser.parseMinutesString(any())
            } throws NumberFormatException("cause")

            val throwable = catchThrowable {
                parkingFeeSettingsRepository.maxAgeBeforePaymentExpiry
            }

            assertThat(throwable)
                .isInstanceOf(IllegalSettingException::class.java)
                .hasMessageContaining("Max Age Before Payment Expiry (minutes)")
                .hasCauseExactlyInstanceOf(NumberFormatException::class.java)
                .hasRootCauseMessage("cause")
        }

        @Test
        fun `When DurationSettingParser parses successfully, then returns result from DurationSettingParser`() {
            every {
                settingsRepositoryHelper.getValue("max_age_before_payment_expiry_in_minutes")
            } returns "100"

            every {
                durationSettingParser.parseMinutesString("100")
            } returns Duration.ofMinutes(100)

            assertThat(parkingFeeSettingsRepository.maxAgeBeforePaymentExpiry)
                .isEqualTo(Duration.ofMinutes(100))
        }
    }
}