package com.johngachihi.parking.services.payment

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.entities.payment.PaymentSession
import com.johngachihi.parking.entities.visit.isInExitAllowancePeriod
import com.johngachihi.parking.repositories.payment.PaymentRepository
import com.johngachihi.parking.repositories.payment.PaymentSessionRepository
import com.johngachihi.parking.repositories.settings.PaymentSettingsRepository
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import com.johngachihi.parking.services.ParkingFeeCalculatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

interface PaymentService {
    /**
     * @throws IllegalPaymentAttemptException
     * @throws InvalidTicketCodeException
     */
    fun startPayment(startPaymentDto: StartPaymentDto): PaymentSession

    /**
     * @throws IllegalPaymentAttemptException
     */
    fun completePayment(completePaymentDto: CompletePaymentDto)
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
    private val paymentSessionRepo: PaymentSessionRepository
) : PaymentService {
    // TODO: Add idempotency
    override fun startPayment(startPaymentDto: StartPaymentDto): PaymentSession {
        val ongoingVisit = ongoingVisitRepo.findByTicketCode(startPaymentDto.ticketCode)
            ?: throw InvalidTicketCodeException(
                "The ticket code provided (${startPaymentDto.ticketCode}) is not for an ongoing visit"
            )

        val maxAgeBeforePaymentExpiry = paymentSettingsRepository.maxAgeBeforePaymentExpiry
        if (ongoingVisit.isInExitAllowancePeriod(maxAgeBeforePaymentExpiry)) {
            throw IllegalPaymentAttemptException(
                "A payment cannot be made for a visit that is in an exit allowance period"
            )
        }

        val parkingFee = parkingFeeCalculatorService.calculateFee(ongoingVisit)
        return paymentSessionRepo.save(PaymentSession().apply {
            amount = parkingFee
            status = PaymentSession.Status.PENDING
            visit = ongoingVisit
        })
    }

    override fun completePayment(completePaymentDto: CompletePaymentDto) {
        val paymentSession = paymentSessionRepo.findByIdOrNull(completePaymentDto.paymentSessionId)
            ?: throw IllegalPaymentAttemptException("Attempted to complete a non-existent payment session")

        if (paymentSession.status != PaymentSession.Status.PENDING) {
            throw IllegalPaymentAttemptException(
                "Attempted to complete a payment session that is ${paymentSession.status}"
            )
        }

        // Checks paymentSession's age using its startedAt property
        // in case the paymentSession should be, but has not yet,
        // been updated to status EXPIRED
        if (paymentSession.isTooOld()) {
            throw IllegalPaymentAttemptException(
                "Attempted to complete a payment session that is EXPIRED"
            )
        }

        paymentRepository.save(Payment().apply {
            visit = paymentSession.visit
            amount = paymentSession.amount
        })
    }

    // TODO: Better name
    fun PaymentSession.isTooOld(): Boolean {
        val age = startedAt.until(Instant.now(), ChronoUnit.MINUTES)

        return age > paymentSettingsRepository.maxAgeBeforePaymentSessionExpiry.toMinutes()
    }
}