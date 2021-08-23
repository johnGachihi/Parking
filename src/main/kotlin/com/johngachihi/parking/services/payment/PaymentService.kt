package com.johngachihi.parking.services.payment

import com.johngachihi.parking.entities.payment.Payment

interface PaymentService {
    /**
     * @throws VisitInExitAllowancePeriodException
     * @throws InvalidTicketCodeException
     */
    fun startPayment(startPaymentDto: StartPaymentDto): Payment
}