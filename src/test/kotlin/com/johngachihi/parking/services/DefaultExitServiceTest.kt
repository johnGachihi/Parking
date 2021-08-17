package com.johngachihi.parking.services

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.UnpaidFeeException
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Test DefaultExitService")
@ExtendWith(MockKExtension::class)
internal class DefaultExitServiceTest {
    @RelaxedMockK
    private lateinit var ongoingVisitRepository: OngoingVisitRepository

    @MockK
    private lateinit var parkingFeeCalculatorService: ParkingFeeCalculatorService

    @InjectMockKs
    private lateinit var exitService: DefaultExitService

    @Nested
    @DisplayName("Test finishVisit()")
    inner class TestFinishVisit {
        @Test
        fun `When the provided ticket-code is not for an OngoingVisit, throws InvalidTicketCodeException`() {
            every {
                ongoingVisitRepository.findByTicketCode(321)
            } returns null

            assertThatExceptionOfType(InvalidTicketCodeException::class.java)
                .isThrownBy { exitService.finishVisit(321) }
                .withMessage("The ticket code provided is not for an ongoing visit.")
        }

        @Nested
        @DisplayName("When the provided ticket-code is for an OngoingVisit")
        inner class TestWhenProvidedTicketCodeIsForOngoingVisit {
            private val ongoingVisit = OngoingVisit()

            @BeforeEach
            fun init() {
                every {
                    ongoingVisitRepository.findByTicketCode(123)
                } returns ongoingVisit
            }

            @Test
            fun `And the OngoingVisit's due parking fee is greater than 0, then throws UnpaidFeeException`() {
                val parkingFee = 122.0
                every {
                    parkingFeeCalculatorService.calculateFee(ongoingVisit)
                } returns parkingFee

                assertThatExceptionOfType(UnpaidFeeException::class.java)
                    .isThrownBy { exitService.finishVisit(123) }
                    .withMessage(
                        "Fee for visit with ticket-code ${ongoingVisit.ticketCode}" +
                                "is not fully paid for. Balance: $parkingFee"
                    )
            }

            @Test
            fun `And the OngoingVisit has no due parking fee, then ends the OngoingVisit`() {
                every {
                    parkingFeeCalculatorService.calculateFee(ongoingVisit)
                } returns 0.0

                exitService.finishVisit(123)

                verify {
                    ongoingVisitRepository.finishOngoingVisit(ongoingVisit)
                }
            }
        }
    }
}