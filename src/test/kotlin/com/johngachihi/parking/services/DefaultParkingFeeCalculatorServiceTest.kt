package com.johngachihi.parking.services

import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.visit.timeOfStay
import com.johngachihi.parking.minutesAgo
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.Instant

@DisplayName("Test DefaultParkingFeeCalculatorService")
@ExtendWith(MockKExtension::class)
internal class DefaultParkingFeeCalculatorServiceTest {
    @MockK
    private lateinit var parkingTariffService: ParkingTariffService

    @MockK
    private lateinit var paymentSettingsRepository: PaymentSettingsRepository

    @InjectMockKs
    private lateinit var parkingFeeCalculatorService: DefaultParkingFeeCalculatorService

    @Nested
    @DisplayName("When no payments have been made for the OngoingVisit")
    inner class TestWhenNoPaymentsHaveBeenMade {
        private val ongoingVisitWithoutPayments = OngoingVisit().apply {
            entryTime = 27.minutesAgo
        }

        @Test
        @DisplayName(
            "Then returns fee directly from the parking-tariffs " +
                    "(ParkingTariffService#getFee) and the OngoingVisit's time-of-stay"
        )
        fun testReturnsFeeDirectlyFromParkingTariffs() {
            val expectedFee = 9898.0
            every {
                parkingTariffService.getFee(ongoingVisitWithoutPayments.timeOfStay)
            } returns expectedFee

            val actualFee = parkingFeeCalculatorService.calculateFee(ongoingVisitWithoutPayments)

            assertThat(actualFee).isEqualTo(expectedFee)
        }
    }

    @Nested
    @DisplayName("When at least one payment has been made for the OngoingVisit")
    inner class TestWhenAtLeastOnePaymentHasBeenMadeForTheOngoingVisit {
        @Test
        @DisplayName(
            "and the latest payment made has expired, " +
                    "then returns a fee as received from the parking-tariffs " +
                    "(ParkingTariffService#getFee) using the OngoingVisit's time-of-stay " +
                    "less the total amount already paid: " +
                    "parkingTariff(ongoingVisit.timeOfStay) - ongoingVisit.totalAmountPaid"
        )
        fun testWhenLatestPaymentHasExpired() {
            val ongoingVisit = OngoingVisit().apply {
                entryTime = 20.minutesAgo
                payments = listOf(
                    makePayment(20.minutesAgo, 1.0),
                    makePayment(10.minutesAgo, 1.0)
                )
            }

            every {
                paymentSettingsRepository.maxAgeBeforePaymentExpiry
            } returns Duration.ofMinutes(5)

            every {
                parkingTariffService.getFee(ongoingVisit.timeOfStay)
            } returns 10.0

            val fee = parkingFeeCalculatorService.calculateFee(ongoingVisit)

            assertThat(fee).isEqualTo(10.0 - (1.0 + 1.0))
        }

        @Test
        fun `And latest payment has not expired, then returns 0`() {
            val ongoingVisit = OngoingVisit().apply {
                payments = listOf(
                    makePayment(10.minutesAgo)
                )
            }

            every {
                paymentSettingsRepository.maxAgeBeforePaymentExpiry
            } returns Duration.ofMinutes(20)

            val fee = parkingFeeCalculatorService.calculateFee(ongoingVisit)

            assertThat(fee).isEqualTo(0.0)
        }

        private fun makePayment(madeAt: Instant, amount: Double = 100.0) =
            Payment().apply {
                this.amount = amount
                this.finishedAt = madeAt
            }
    }
}