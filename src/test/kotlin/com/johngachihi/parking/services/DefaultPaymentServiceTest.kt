package com.johngachihi.parking.services

import com.johngachihi.parking.entities.OngoingVisit
import com.johngachihi.parking.entities.visit.timeOfStay
import com.johngachihi.parking.minutesAgo
import com.johngachihi.parking.repositories.ParkingFeeConfigRepo
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Test DefaultPaymentService")
@ExtendWith(MockKExtension::class)
internal class DefaultPaymentServiceTest {
    @MockK
    private lateinit var parkingTariffService: ParkingTariffService

    @MockK
    private lateinit var parkingFeeConfigRepo: ParkingFeeConfigRepo

    @InjectMockKs
    private lateinit var paymentService: DefaultPaymentService

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

            val actualFee = paymentService.calculateParkingFee(ongoingVisitWithoutPayments)

            assertThat(actualFee).isEqualTo(expectedFee)
        }
    }

    @Test
    @DisplayName(
        "When at least one payment has been made for the OngoingVisit " +
                "and the latest payment made has expired, " +
                "then returns a fee as received from the parking-tariffs " +
                "(ParkingTariffService#getFee) using the OngoingVisit's time-of-stay " +
                "less the total amount already paid."
    )
    fun testWhenLatestPaymentHasExpired() {

    }
}