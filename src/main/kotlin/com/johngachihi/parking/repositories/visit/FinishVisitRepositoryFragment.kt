package com.johngachihi.parking.repositories.visit

import com.johngachihi.parking.entities.visit.FinishedVisit
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.entities.payment.Payment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import javax.persistence.EntityManager

interface FinishVisitRepositoryFragment {
    fun finishOngoingVisit(ongoingVisit: OngoingVisit)
}

@Component
class FinishVisitRepositoryFragmentImpl(
    @Autowired
    private val entityManager: EntityManager
) : FinishVisitRepositoryFragment {
    override fun finishOngoingVisit(ongoingVisit: OngoingVisit) {
        val finishedVisit = FinishedVisit().apply {
            ticketCode = ongoingVisit.ticketCode
            entryTime = ongoingVisit.entryTime
            exitTime = Instant.now()
        }
        entityManager.persist(finishedVisit)

        val paymentsBuffer = mutableListOf<Payment>()
        for (payment in ongoingVisit.payments) {
            payment.visit = finishedVisit
            paymentsBuffer.add(payment)
        }

        finishedVisit.payments = paymentsBuffer

        entityManager.remove(ongoingVisit)
    }
}