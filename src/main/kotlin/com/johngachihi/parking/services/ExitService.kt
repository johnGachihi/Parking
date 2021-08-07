package com.johngachihi.parking.services

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.UnpaidFeeException
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface ExitService {
    /**
     * @throws InvalidTicketCodeException
     * @throws UnpaidFeeException
     */
    fun finishVisit(ticketCode: Long)
}

@Service
class DefaultExitService(
    @Autowired
    private val ongoingVisitRepository: OngoingVisitRepository,
    @Autowired
    private val paymentService: PaymentService
) : ExitService {
    override fun finishVisit(ticketCode: Long) {
        val ongoingVisit = ongoingVisitRepository.findByTicketCode(ticketCode)
            ?: throw InvalidTicketCodeException(
                "The ticket code provided is not for an ongoing visit."
            )

        val parkingFee = paymentService.calculateParkingFee(ongoingVisit)
        if (parkingFee > 0) {
            throw UnpaidFeeException(
                "Fee for visit with ticket-code ${ongoingVisit.ticketCode}" +
                        "is not fully paid for. Balance: $parkingFee"
            )
        }

        ongoingVisitRepository.finishOngoingVisit(ongoingVisit)
    }
}