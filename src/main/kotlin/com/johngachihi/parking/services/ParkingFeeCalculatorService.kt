package com.johngachihi.parking.services

import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.payment.isExpired
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.entities.visit.timeOfStay
import com.johngachihi.parking.entities.visit.totalAmountPaid
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface ParkingFeeCalculatorService {
    fun calculateFee(ongoingVisit: OngoingVisit): Double
}

@Service
class DefaultParkingFeeCalculatorService(
    @Autowired
    private val parkingTariffService: ParkingTariffService,
    @Autowired
    private val paymentSettingsRepository: PaymentSettingsRepository
) : ParkingFeeCalculatorService {
    override fun calculateFee(ongoingVisit: OngoingVisit): Double {
        // Use entity logic
        if (ongoingVisit.payments.isEmpty())
            return parkingTariffService.getFee(ongoingVisit.timeOfStay)

        val latestPayment = getLatestPayment(ongoingVisit)!!
        if (isExpired(latestPayment)) {
            return parkingTariffService.getFee(ongoingVisit.timeOfStay) - ongoingVisit.totalAmountPaid
        }

        return 0.0
    }

    private fun getLatestPayment(ongoingVisit: OngoingVisit) =
        ongoingVisit.payments.maxByOrNull { it.madeAt }

    private fun isExpired(payment: Payment): Boolean =
        payment.isExpired(paymentSettingsRepository.maxAgeBeforePaymentExpiry)
}