package com.johngachihi.parking.services.payment

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.payment.PaymentSession
import com.johngachihi.parking.entities.visit.isInExitAllowancePeriod
import com.johngachihi.parking.repositories.payment.PaymentRepository
import com.johngachihi.parking.repositories.payment.PaymentSessionRepository
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import com.johngachihi.parking.services.ParkingFeeCalculatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface PaymentService {
    /**
     * @throws IllegalPaymentException
     * @throws InvalidTicketCodeException
     */
    fun startPayment(startPaymentDto: StartPaymentDto): PaymentSession
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
    private val paymentRepository: PaymentRepository,
    @Autowired
    private val paymentSessionRepository: PaymentSessionRepository
) : PaymentService {
    override fun startPayment(startPaymentDto: StartPaymentDto): PaymentSession {
        val ongoingVisit = ongoingVisitRepo.findByTicketCode(startPaymentDto.ticketCode)
            ?: throw InvalidTicketCodeException(
                "The ticket code provided (${startPaymentDto.ticketCode}) is not for an ongoing visit"
            )

        val maxAgeBeforePaymentExpiry = paymentSettingsRepository.maxAgeBeforePaymentExpiry
        if (ongoingVisit.isInExitAllowancePeriod(maxAgeBeforePaymentExpiry)) {
            throw IllegalPaymentException(
                "A payment cannot be made for a visit that is in an exit allowance period"
            )
        }

        val parkingFee = parkingFeeCalculatorService.calculateFee(ongoingVisit)
        return paymentSessionRepository.save(PaymentSession().apply {
            amount = parkingFee
            status = PaymentSession.Status.PENDING
            visit = ongoingVisit
        })
    }
}