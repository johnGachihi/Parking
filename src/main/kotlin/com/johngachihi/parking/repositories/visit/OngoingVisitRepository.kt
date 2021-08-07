package com.johngachihi.parking.repositories.visit

import com.johngachihi.parking.entities.visit.OngoingVisit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OngoingVisitRepository : JpaRepository<OngoingVisit, Long>, FinishVisitRepositoryFragment {
//    fun saveOnGoingVisit(ongoingVisit: OngoingVisit): Long
//    Replaced by save(...)

    //    fun onGoingVisitExistsWithTicketCode(ticketCode: Long): Boolean
    fun existsByTicketCode(ticketCode: Long): Boolean

//    fun findOngoingVisitByTicketCode(ticketCode: Long): OngoingVisit?
    fun findByTicketCode(ticketCode: Long): OngoingVisit?

//    fun finishOngoingVisit(ongoingVisit: OngoingVisit) // TODO: Add query
//    Replaced by FinishVisitRepositoryFragment
}