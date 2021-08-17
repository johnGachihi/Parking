package com.johngachihi.parking.entities.payment

import com.johngachihi.parking.minutes
import com.johngachihi.parking.minutesAgo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Test Payment entity's extension functions and variables")
internal class PaymentExtKtTest {
    @Nested
    @DisplayName("Test isExpired()")
    inner class TestIsExpired {
        @Test
        fun `When the payment is older than the maxAgeBeforePaymentExpiry, then returns true`() {
            val payment = Payment().apply {
                madeAt = 20.minutesAgo
            }
            val maxAgeBeforePaymentExpiry = 10.minutes

            assertThat(payment.isExpired(maxAgeBeforePaymentExpiry))
                .isTrue()
        }

        @Test
        fun `When the payment is not older than the maxAgeBeforePaymentExpiry, then returns false`() {
            val payment = Payment().apply {
                madeAt = 10.minutesAgo
            }
            val maxAgeBeforePaymentExpiry = 20.minutes

            assertThat(payment.isExpired(maxAgeBeforePaymentExpiry))
                .isFalse()
        }

        @Test
        fun `When the payment's age is the same as the maxAgeBeforePaymentExpiry, then returns false`() {
            val payment = Payment().apply {
                madeAt = 10.minutesAgo
            }
            val maxAgeBeforePaymentExpiry = 10.minutes

            assertThat(payment.isExpired(maxAgeBeforePaymentExpiry))
                .isFalse()
        }
    }
}