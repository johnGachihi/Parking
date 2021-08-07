package com.johngachihi.parking.repositories

import com.johngachihi.parking.entities.OngoingVisit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OngoingVisitRepository : JpaRepository<OngoingVisit, Long> {
//    fun saveOnGoingVisit(ongoingVisit: OngoingVisit): Long
//    Replaced by save(...)

    //    fun onGoingVisitExistsWithTicketCode(ticketCode: Long): Boolean
    fun existsByTicketCode(ticketCode: Long): Boolean

//    fun findOngoingVisitByTicketCode(ticketCode: Long): OngoingVisit?
    fun findByTicketCode(ticketCode: Long): OngoingVisit?

    fun finishOngoingVisit(ongoingVisit: OngoingVisit) // TODO: Add query
}