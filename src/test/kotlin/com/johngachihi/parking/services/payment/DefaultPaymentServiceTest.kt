package com.johngachihi.parking.services.payment

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.payment.PaymentSession
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.minutes
import com.johngachihi.parking.minutesAgo
import com.johngachihi.parking.repositories.payment.PaymentRepository
import com.johngachihi.parking.repositories.payment.PaymentSessionRepository
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
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
internal class DefaultPaymentServiceTest {
    @MockK
    private lateinit var ongoingVisitRepository: OngoingVisitRepository

    @MockK
    private lateinit var paymentSettingsRepository: PaymentSettingsRepository

    @MockK
    private lateinit var paymentRepository: PaymentRepository

    @MockK
    private lateinit var paymentSessionRepository: PaymentSessionRepository

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
            "When ticketCode provided is for an OngoingVisit in an exit-allowance " +
                    "period, then throw IllegalPaymentAttemptException"
        )
        fun testWhenOngoingVisitIsInExitAllowancePeriod() {
            val startPaymentDto = StartPaymentDto(ticketCode = 1234L)
            every {
                ongoingVisitRepository.findByTicketCode(startPaymentDto.ticketCode)
            } returns OngoingVisit().apply {
                payments = listOf(Payment().apply { madeAt = 10.minutesAgo })
            } // Returns an OngoingVisit whose latest payment was made 10 minutes ago

            every {
                paymentSettingsRepository.maxAgeBeforePaymentExpiry
            } returns 20.minutes
            // The maximum age of a payment before it expires is 20 minutes

            assertThatExceptionOfType(IllegalPaymentAttemptException::class.java)
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
                    paymentSessionRepository.save(any())
                } returns PaymentSession()

                every {
                    parkingFeeCalculatorService.calculateFee(ongoingVisit)
                } returns 0.0
            }

            @Test
            @DisplayName(
                "then creates and stores a new PaymentSession with " +
                        "'amount' as calculated by ParkingFeeCalculatorService"
            )
            fun thenCreatesAndStoresANewPaymentSessionWithAppropriateAmount() {
                every {
                    parkingFeeCalculatorService.calculateFee(ongoingVisit)
                } returns 987.0

                paymentService.startPayment(startPaymentDto)

                verify {
                    paymentSessionRepository.save(match { it.amount == 987.0 })
                }
            }

            @Test
            fun `then creates and stores a new PaymentSession with status PENDING`() {
                paymentService.startPayment(startPaymentDto)

                verify {
                    paymentSessionRepository.save(match { it.status == PaymentSession.Status.PENDING })
                }
            }

            @Test
            @DisplayName(
                "then creates and stores a new PaymentSession with 'visit' " +
                        "property being the OngoingVisit for the ticket code provided"
            )
            fun testCreatedPaymentVisitProperty() {
                paymentService.startPayment(startPaymentDto)

                verify {
                    paymentSessionRepository.save(match { it.visit == ongoingVisit })
                }
            }

            @Test
            fun `then returns created and persisted PaymentSession`() {
                val expectedPaymentSession = PaymentSession()
                every {
                    paymentSessionRepository.save(any())
                } returns expectedPaymentSession

                val actualPaymentSession = paymentService.startPayment(startPaymentDto)

                assertThat(actualPaymentSession).isEqualTo(expectedPaymentSession)
            }
        }
    }

    @Nested
    @DisplayName("Test completePayment()")
    inner class TestCompletePaymentMethod {
        private val completePaymentDto: CompletePaymentDto = CompletePaymentDto(1234)

        @BeforeEach
        fun init() {
            every { paymentRepository.save(any()) } returns Payment()
        }

        @Test
        @DisplayName(
            "When payment-session-id provided does not belong to an existing PaymentSession, " +
                    "then throws IllegalPaymentAttemptException with appropriate message"
        )
        fun testWhenPaymentSessionIdProvidedDoesNotBelongToExistingPaymentSession() {
            every {
                paymentSessionRepository.findByIdOrNull(completePaymentDto.paymentSessionId)
            } returns null

            assertThatExceptionOfType(IllegalPaymentAttemptException::class.java)
                .isThrownBy { paymentService.completePayment(completePaymentDto) }
                .withMessage("Attempted to complete a non-existent payment session")
        }

        @Test
        @DisplayName(
            "When paymentSessionId provided belongs to a PaymentSession " +
                    "having a status that is not PENDING, " +
                    "then throws an IllegalPaymentAttemptException with appropriate message"
        )
        fun testWhenPaymentSessionIdProvidedDoesNotBelongToAPendingPaymentSession() {
            val paymentSession = PaymentSession()

            every {
                paymentSessionRepository.findByIdOrNull(completePaymentDto.paymentSessionId)
            } answers {
                paymentSession.status = PaymentSession.Status.COMPLETED
                paymentSession
            } andThenAnswer {
                paymentSession.status = PaymentSession.Status.CANCELLED
                paymentSession
            } andThenAnswer {
                paymentSession.status = PaymentSession.Status.EXPIRED
                paymentSession
            }

            for (i in 1..3) {
                assertThatExceptionOfType(IllegalPaymentAttemptException::class.java)
                    .isThrownBy { paymentService.completePayment(completePaymentDto) }
                    .withMessage("Attempted to complete a payment session that is ${paymentSession.status}")
            }
        }

        @Test
        @DisplayName(
            "When provided paymentSessionId is for a PaymentSession marked PENDING " +
                    "but that is older than the set max-payment-session-age, " +
                    "then throws IllegalPaymentAttemptException with appropriate message"
        )
        fun testWhenPaymentSessionTooOld() {
            every {
                paymentSessionRepository.findByIdOrNull(completePaymentDto.paymentSessionId)
            } returns PaymentSession().apply {
                startedAt = 30.minutesAgo
                status = PaymentSession.Status.PENDING
            }

            every {
                paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry
            } returns 20.minutes

            assertThatExceptionOfType(IllegalPaymentAttemptException::class.java)
                .isThrownBy { paymentService.completePayment(completePaymentDto) }
                .withMessage("Attempted to complete a payment session that is EXPIRED")
        }

        @Test
        @DisplayName(
            "When the PaymentSession is valid, " +
                    "then create and persist a Payment with the " +
                    "amount and visit fields from the PaymentSession"
        )
        fun testWhenPaymentSessionValid() {
            val ongoingVisit = OngoingVisit()

            // GIVEN: The paymentSessionId provided is for a PaymentSession that:
            //        - Exists,
            //        - Is PENDING, and
            //        - Is not older than the maxAgeBeforePaymentSessionExpiry
            every {
                paymentSessionRepository.findByIdOrNull(completePaymentDto.paymentSessionId)
            } returns PaymentSession().apply {
                status = PaymentSession.Status.PENDING
                startedAt = 10.minutesAgo
                amount = 100.0
                visit = ongoingVisit
            }

            every {
                paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry
            } returns 20.minutes

            // WHEN
            paymentService.completePayment(completePaymentDto)

            // THEN
            verify {
                paymentRepository.save(match {
                    it.visit == ongoingVisit && it.amount == 100.0
                })
            }
        }
    }

    @Nested
    @DisplayName("Test cancelPayment()")
    inner class TestCancelPaymentMethod {
        @Test
        @DisplayName(
            "When paymentSessionId provided is for non-existent PaymentSession, " +
                    "then throws IllegalPaymentCancellationException"
        )
        fun testWhenPaymentSessionIdProvidedIsForNonExistentPaymentSession() {
            val paymentSessionId = 123L

            every {
                paymentSessionRepository.findByIdOrNull(paymentSessionId)
            } returns null

            assertThatExceptionOfType(IllegalPaymentCancellationAttemptException::class.java)
                .isThrownBy { paymentService.cancelPayment(paymentSessionId) }
                .withMessage("Attempted to cancel a non-existent payment-session")
        }

        @Test
        @DisplayName(
            "When PaymentSession being cancelled is not PENDING, " +
                    "then throws IllegalPaymentCancellationException"
        )
        fun testWhenPaymentSessionIsNotPending() {
            val paymentSessionId = 123L

            every {
                paymentSessionRepository.findByIdOrNull(paymentSessionId)
            } returns PaymentSession().apply {
                status = PaymentSession.Status.COMPLETED
            }

            assertThatExceptionOfType(IllegalPaymentCancellationAttemptException::class.java)
                .isThrownBy { paymentService.cancelPayment(paymentSessionId) }
                .withMessage("Attempted to cancel an ended payment session")
        }

        @Test
        @DisplayName(
            "When PaymentSession is marked PENDING but is expired, " +
                    "then throws IllegalPaymentCancellationException"
        )
        fun testWhenPaymentSessionIsMarkedPendingButIsExpired() {
            val paymentSessionId = 123L

            every {
                paymentSessionRepository.findByIdOrNull(paymentSessionId)
            } returns PaymentSession().apply {
                startedAt = 20.minutesAgo
                status = PaymentSession.Status.PENDING
            }

            every {
                paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry
            } returns 10.minutes

            assertThatExceptionOfType(IllegalPaymentCancellationAttemptException::class.java)
                .isThrownBy { paymentService.cancelPayment(paymentSessionId) }
                .withMessage("Attempted to cancel an ended payment session")
        }

        @Test
        fun `When PaymentSession is truly PENDING, then updates it to CANCELLED`() {
            val paymentSessionId = 123L

            every {
                paymentSessionRepository.findByIdOrNull(paymentSessionId)
            } returns PaymentSession().apply {
                startedAt = 20.minutesAgo
                status = PaymentSession.Status.PENDING
            }

            every {
                paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry
            } returns 25.minutes

            every { paymentSessionRepository.save(any()) } returns PaymentSession()

            paymentService.cancelPayment(paymentSessionId)

            verify {
                paymentSessionRepository.save(match {
                    it.status == PaymentSession.Status.CANCELLED
                })
            }
        }
    }
}
