package com.johngachihi.parking.repositories

import com.johngachihi.parking.entities.visit.FinishedVisit
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.repositories.visit.FinishVisitRepositoryFragmentImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DisplayName("Test FinishVisitRepositoryFragmentImpl")
@DataJpaTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
    ]
)
internal class FinishVisitRepositoryFragmentImplTest {
    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var finishVisitRepository: FinishVisitRepositoryFragmentImpl

    private lateinit var ongoingVisit: OngoingVisit

    @BeforeEach
    fun init() {
        ongoingVisit = entityManager.persistAndFlush(
            OngoingVisit().apply {
                ticketCode = 1234
            })

        ongoingVisit.payments = listOf(
            Payment().apply {
                amount = 111.1
                visit = ongoingVisit;
                status = Payment.Status.COMPLETED
            },
            Payment().apply {
                amount = 111.1
                visit = ongoingVisit;
                status = Payment.Status.COMPLETED
            }
        )

        entityManager.persistAndFlush(ongoingVisit)
    }

    @Test
    fun `Deletes the OngoingVisit`() {
        finishVisitRepository.finishOngoingVisit(ongoingVisit)

        assertThat(entityManager.find(OngoingVisit::class.java, ongoingVisit.id))
            .isNull()
    }

    @DataJpaTest(
        properties = [
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.flyway.enabled=false"
        ]
    )
    @Nested
    @DisplayName("Persists a new FinishedVisit")
    inner class TestPersistsNewFinishedVisit {
        @Test
        fun `With the same 'entryTime' and 'ticketCode' as the deleted OngoingVisit`() {
            finishVisitRepository.finishOngoingVisit(ongoingVisit)

            val finishedVisit = assertThatThereIsOnlyOneFinishedVisitInDBAndReturnIt()

            assertThat(finishedVisit.entryTime).isEqualTo(ongoingVisit.entryTime)
            assertThat(finishedVisit.ticketCode).isEqualTo(ongoingVisit.ticketCode)
        }

        @Test
        fun `With the same Payments as the deleted OngoingVisit`() {
            finishVisitRepository.finishOngoingVisit(ongoingVisit)

            val finishedVisit = assertThatThereIsOnlyOneFinishedVisitInDBAndReturnIt()

            finishedVisit.payments.zip(ongoingVisit.payments) { fPayment, oPayment ->
                assertThat(fPayment).isEqualTo(oPayment)
            }
        }

        @Test
        fun `With Payments that have the new FinishedVisit as their 'owner'`() {
            finishVisitRepository.finishOngoingVisit(ongoingVisit)

            val finishedVisit = assertThatThereIsOnlyOneFinishedVisitInDBAndReturnIt()

            finishedVisit.payments.forEach {
                assertThat(it.visit).isEqualTo(finishedVisit)
            }
        }

        private fun assertThatThereIsOnlyOneFinishedVisitInDBAndReturnIt(): FinishedVisit {
            val finishedVisits = entityManager.entityManager.createQuery(
                "select f from FinishedVisit f",
                FinishedVisit::class.java
            ).resultList

            assertThat(finishedVisits.size)
                .isEqualTo(1)
                .withFailMessage("The number of FinishedVisits in the DB is not 1")

            return finishedVisits.first()
        }
    }
}