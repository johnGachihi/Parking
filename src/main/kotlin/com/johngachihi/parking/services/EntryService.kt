package com.johngachihi.parking.services

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface EntryService {
    /**
     * @throws InvalidTicketCodeException
     */
    fun addVisit(ticketCode: Long)
}

@Service
class DefaultEntryService(
    @Autowired private val ongoingVisitRepository: OngoingVisitRepository
) : EntryService {
    override fun addVisit(ticketCode: Long) {
        if (isTicketCodeIsInUse(ticketCode)) {
            throw InvalidTicketCodeException("The ticket code provided ($ticketCode) is already in use")
        }

        val newVisit = OngoingVisit().apply { this.ticketCode = ticketCode }
        ongoingVisitRepository.save(newVisit)
    }

    private fun isTicketCodeIsInUse(ticketCode: Long): Boolean =
        ongoingVisitRepository.existsByTicketCode(ticketCode)
}