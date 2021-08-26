package com.johngachihi.parking.services.payment

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.minutes
import com.johngachihi.parking.minutesAgo
import com.johngachihi.parking.repositories.PaymentRepository
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import com.johngachihi.parking.services.ParkingFeeCalculatorService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DefaultPaymentServiceTest {
    @MockK
    private lateinit var ongoingVisitRepository: OngoingVisitRepository

    @MockK
    private lateinit var paymentSettingsRepository: PaymentSettingsRepository

    @MockK
    private lateinit var paymentRepository: PaymentRepository

    @MockK
    private lateinit var parkingFeeCalculatorService: ParkingFeeCalculatorService

    @InjectMockKs
    private lateinit var paymentService: DefaultPaymentService

    @Nested
    @DisplayName("Test startPayment()")
    inner class TestStartPaymentMethod {
        @Test
        fun `When ticketCode provided is not for an OngoingVisit, then throw InvalidTicketCodeException`() {
            val startPaymentDto = StartPaymentDto(ticketCode = 1234L)
            every {
                ongoingVisitRepository.findByTicketCode(startPaymentDto.ticketCode)
            } returns null

            assertThatExceptionOfType(InvalidTicketCodeException::class.java)
                .isThrownBy { paymentService.startPayment(startPaymentDto) }
                .withMessage("The ticket code provided (${startPaymentDto.ticketCode}) is not for an ongoing visit")
        }

        @Test
        @DisplayName(
            "When ticketCode provided is for an OngoingVisit in a exit-allowance " +
                    "period, then throw IllegalPaymentException"
        )
        fun testWhenOngoingVisitIsInExitAllowancePeriod() {
            val startPaymentDto = StartPaymentDto(ticketCode = 1234L)
            every {
                ongoingVisitRepository.findByTicketCode(startPaymentDto.ticketCode)
            } returns OngoingVisit().apply {
                payments = listOf(Payment().apply { finishedAt = 10.minutesAgo })
            }

            every {
                paymentSettingsRepository.maxAgeBeforePaymentExpiry
            } returns 20.minutes

            assertThatExceptionOfType(IllegalPaymentException::class.java)
                .isThrownBy { paymentService.startPayment(startPaymentDto) }
                .withMessage("A payment cannot be made for a visit that is in an exit allowance period")
        }

        @Nested
        @DisplayName("When ticket code is valid")
        inner class TestWhenTicketCodeValid {
            private val startPaymentDto = StartPaymentDto(ticketCode = 1234L)
            private val ongoingVisit = OngoingVisit()

            @BeforeEach
            fun init() {
                every {
                    ongoingVisitRepository.findByTicketCode(startPaymentDto.ticketCode)
                } returns ongoingVisit

                every {
                    paymentSettingsRepository.maxAgeBeforePaymentExpiry
                } returns 20.minutes

                every {
                    paymentRepository.save(any())
                } returns Payment()

                every {
                    parkingFeeCalculatorService.calculateFee(ongoingVisit)
                } returns 0.0
            }

            @Test
            fun `then creates and stores a new Payment with 'amount' as calculated by ParkingFeeCalculatorService`() {
                every {
                    parkingFeeCalculatorService.calculateFee(ongoingVisit)
                } returns 987.0

                paymentService.startPayment(startPaymentDto)

                verify {
                    paymentRepository.save(match { it.amount == 987.0 })
                }
            }

            @Test
            fun `then creates and stores a new Payment with status STARTED`() {
                paymentService.startPayment(startPaymentDto)

                verify {
                    paymentRepository.save(match { it.status == Payment.Status.STARTED })
                }
            }

            @Test
            @DisplayName(
                "then creates and stores a new Payment with 'visit' " +
                        "property being the OngoingVisit for the ticket code provided"
            )
            fun testCreatedPaymentVisitProperty() {
                paymentService.startPayment(startPaymentDto)

                verify {
                    paymentRepository.save(match { it.visit == ongoingVisit })
                }
            }

            @Test
            fun `then returns created and persisted Payment`() {
                val expectedPayment = Payment()
                every {
                    paymentRepository.save(any())
                } returns expectedPayment

                val actualPayment = paymentService.startPayment(startPaymentDto)

                assertThat(actualPayment).isEqualTo(expectedPayment)
            }
        }
    }
}