package com.johngachihi.parking.web.payment

import com.johngachihi.parking.entities.payment.PaymentSession
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.entities.visit.latestPaymentOrNull
import com.johngachihi.parking.entities.visit.timeOfStay
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StartPaymentOutputDtoAssembler(
    @Autowired
    private val paymentSettingsRepository: PaymentSettingsRepository
) {
    fun assemble(paymentSession: PaymentSession): StartPaymentOutputDto {
        val maxAgeBeforePaymentSessionExpiry =
            paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry

        val ongoingVisit = paymentSession.visit as? OngoingVisit
            ?: throw IllegalArgumentException(
                "The visit property of the PaymentSession argument " +
                        "should be an instance of OngoingVisit"
            )

        return StartPaymentOutputDto(
            paymentSession.id!!,
            paymentSession.startedAt.plus(maxAgeBeforePaymentSessionExpiry),
            paymentSession.amount!!,
            ongoingVisit.timeOfStay,
            ongoingVisit.entryTime,
            ongoingVisit.latestPaymentOrNull?.madeAt
        )
    }
}
