package com.johngachihi.parking.entities.visit

import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.minutes
import com.johngachihi.parking.minutesAgo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration

@DisplayName("Test OngoingVisit entity extension functions and variables")
internal class OngoingVisitExtKtTest {
    @Test
    fun `test timeOfStay`() {
        val ongoingVisit = OngoingVisit().apply {
            entryTime = 20.minutesAgo
        }

        assertThat(ongoingVisit.timeOfStay)
            .isEqualTo(Duration.ofMinutes(20))
    }

    @Test
    fun `test totalAmountPaid`() {
        val ongoingVisit = OngoingVisit().apply {
            payments = listOf(
                Payment().apply { amount = 1.0 },
                Payment().apply { amount = 1.0 },
                Payment().apply { amount = 1.0 }
            )
        }

        assertThat(ongoingVisit.totalAmountPaid)
            .isEqualTo(3.0)
    }

    @Test
    fun `test hasAtLeastOnePayment`() {
        // When: OngoingVisit has 1 payment
        val ongoingVisit1 = OngoingVisit().apply {
            payments = listOf(Payment().apply { amount = 1.0 })
        }
        // Then: returns true
        assertThat(ongoingVisit1.hasAtLeastOnePayment).isTrue()

        // When: OngoingVisit has 2 payment
        val ongoingVisit2 = OngoingVisit().apply {
            payments = listOf(
                Payment().apply { amount = 1.0 },
                Payment().apply { amount = 1.0 }
            )
        }
        // Then: returns true
        assertThat(ongoingVisit2.hasAtLeastOnePayment).isTrue()

        // When: OngoingVisit has 0 payment
        val ongoingVisit3 = OngoingVisit().apply {
            payments = listOf()
        }
        // Then: returns false
        assertThat(ongoingVisit3.hasAtLeastOnePayment).isFalse()
    }

    @Nested
    @DisplayName("test latestPayment")
    inner class TestLatestPayment {
        @Test
        fun `When the OngoingVisit has no payments, throws NoSuchElementException`() {
            val ongoingVisit = OngoingVisit()

            assertThatExceptionOfType(NoSuchElementException::class.java)
                .isThrownBy { ongoingVisit.latestPayment }
        }

        @Test
        fun `When the OngoingVisit has at least one payment, returns the latest one`() {
            val payments = listOf(
                Payment().apply { finishedAt = 10.minutesAgo },
                Payment().apply { finishedAt = 20.minutesAgo },
            )
            val ongoingVisit = OngoingVisit().apply { this.payments = payments }

            assertThat(ongoingVisit.latestPayment)
                .isEqualTo(payments.first())
        }
    }

    @Nested
    @DisplayName("Test isInExitAllowancePeriod")
    inner class TestIsInExitAllowancePeriod {
        @Test
        fun `When the OngoingVisit has no payments, then returns false`() {
            val ongoingVisit = OngoingVisit()

            assertThat(ongoingVisit.isInExitAllowancePeriod(10.minutes))
                .isEqualTo(false)
        }

        @Nested
        @DisplayName("When the OngoingVisit has at least one payment")
        inner class TestWhenHasAtLeastOnePayment {
            @Test
            fun `And the latest payment has expired, then returns false`() {
                val maxAgeBeforePaymentExpiration = 10.minutes
                val ongoingVisit = OngoingVisit().apply {
                    payments = listOf(Payment().apply { finishedAt = 20.minutesAgo })
                }

                assertThat(ongoingVisit.isInExitAllowancePeriod(maxAgeBeforePaymentExpiration))
                    .isEqualTo(false)
            }

            @Test
            fun `And the latest payment has not expired, then returns true`() {
                val maxAgeBeforePaymentExpiration = 20.minutes
                val ongoingVisit = OngoingVisit().apply {
                    payments = listOf(Payment().apply { finishedAt = 10.minutesAgo })
                }

                assertThat(ongoingVisit.isInExitAllowancePeriod(maxAgeBeforePaymentExpiration))
                    .isEqualTo(true)
            }
        }
    }
}