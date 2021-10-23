package com.johngachihi.parking.web.payment

import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.payment.PaymentSession
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.entities.visit.Visit
import com.johngachihi.parking.minutes
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class StartPaymentOutputDtoAssemblerTest {
    @MockK
    private lateinit var paymentSettingsRepository: PaymentSettingsRepository

    @InjectMockKs
    private lateinit var startPaymentOutputDtoAssembler: StartPaymentOutputDtoAssembler

    @Test
    @DisplayName(
        "When PaymentSession has a Visit that is not an instance of OngoingVisit, " +
                "then throws IllegalArgumentException"
    )
    fun testWhenPaymentSessionHasVisitThatIsNotOngoingVisit() {
        every {
            paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry
        } returns 10.minutes

        val paymentSession = PaymentSession().apply {
            id = 1
            amount = 100.0
            visit = Visit()
            status = PaymentSession.Status.PENDING
        }

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { startPaymentOutputDtoAssembler.assemble(paymentSession) }
            .withMessage(
                "The visit property of the PaymentSession argument " +
                        "should be an instance of OngoingVisit"
            )
    }

    @Test
    fun `returns appropriate StartPaymentOutputDto`() {
        every {
            paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry
        } returns 10.minutes

        val paymentSession = PaymentSession().apply {
            id = 1
            amount = 100.0
            visit = OngoingVisit().apply {
                payments = listOf(Payment().apply { madeAt = Instant.parse("2021-11-04T10:15:30.00Z") })
            }
            status = PaymentSession.Status.PENDING
        }

        val startPaymentOutputDto = startPaymentOutputDtoAssembler.assemble(paymentSession)

        assertThat(startPaymentOutputDto.paymentSessionDto.id)
            .isEqualTo(paymentSession.id)

        assertThat(startPaymentOutputDto.paymentSessionDto.paymentAmount)
            .isEqualTo(paymentSession.amount)

        assertThat(startPaymentOutputDto.paymentSessionDto.expiryTime)
            .isEqualTo(paymentSession.startedAt.plus(10.minutes))

        assertThat(startPaymentOutputDto.visit.timeOfStay)
            .isCloseTo(
                Duration.between(paymentSession.visit.entryTime, Instant.now()),
                Duration.ofMinutes(1)
            )

        assertThat(startPaymentOutputDto.visit.entryTime)
            .isEqualTo(paymentSession.visit.entryTime)

        assertThat(startPaymentOutputDto.visit.latestPaymentTime)
            .isEqualTo(paymentSession.visit.payments[0].madeAt)
    }
}
