package com.johngachihi.parking.entities.visit

import com.johngachihi.parking.entities.Payment
import com.johngachihi.parking.minutesAgo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration

@DisplayName("Test OngoingVisit entity extension functions")
internal class OngoingVisitExtKtTest {
    @Test
    fun `test timeOfStay`() {
        val ongoingVisit = OngoingVisit().apply {
            entryTime = 20.minutesAgo
        }

        assertThat(ongoingVisit.timeOfStay)
            .isEqualTo(Duration.ofMinutes(20))
    }

    @Test
    fun `test totalAmountPaid`() {
        val ongoingVisit = OngoingVisit().apply {
            payments = listOf(
                Payment().apply { amount = 1.0 },
                Payment().apply { amount = 1.0 },
                Payment().apply { amount = 1.0 }
            )
        }

        assertThat(ongoingVisit.totalAmountPaid)
            .isEqualTo(3.0)
    }
}