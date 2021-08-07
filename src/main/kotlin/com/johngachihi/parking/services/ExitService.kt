package com.johngachihi.parking.services

interface ExitService {
    /**
     * @throws InvalidTicketCodeException
     * @throws UnpaidFeeException
     */
    fun finishVisit(ticketCode: Long)
}