package com.johngachihi.parking.services.payment

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.visit.isInExitAllowancePeriod
import com.johngachihi.parking.repositories.PaymentRepository
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import com.johngachihi.parking.services.ParkingFeeCalculatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface PaymentService {
    /**
     * @throws VisitInExitAllowancePeriodException
     * @throws InvalidTicketCodeException
     */
    fun startPayment(startPaymentDto: StartPaymentDto): Payment
}


@Service
class DefaultPaymentService(
    @Autowired
    private val ongoingVisitRepo: OngoingVisitRepository,
    @Autowired
    private val paymentSettingsRepository: PaymentSettingsRepository,
    @Autowired
    private val parkingFeeCalculatorService: ParkingFeeCalculatorService,
    @Autowired
    private val paymentRepository: PaymentRepository
) : PaymentService {
    override fun startPayment(startPaymentDto: StartPaymentDto): Payment {
        val ongoingVisit = ongoingVisitRepo.findByTicketCode(startPaymentDto.ticketCode)
            ?: throw InvalidTicketCodeException(
                "The ticket code provided (${startPaymentDto.ticketCode}) is not for an ongoing visit"
            )

        val maxAgeBeforePaymentExpiry = paymentSettingsRepository.maxAgeBeforePaymentExpiry
        if (ongoingVisit.isInExitAllowancePeriod(maxAgeBeforePaymentExpiry)) {
            throw VisitInExitAllowancePeriodException()
        }

        val parkingFee = parkingFeeCalculatorService.calculateFee(ongoingVisit)
        val payment = Payment().apply {
            amount = parkingFee
            status = Payment.Status.STARTED
            visit = ongoingVisit
        }
        return paymentRepository.save(payment)
    }
}