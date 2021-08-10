package com.johngachihi.parking.services

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Test DefaultEntryService")
@ExtendWith(MockKExtension::class)
internal class DefaultEntryServiceTest {
    @MockK
    private lateinit var ongoingVisitRepository: OngoingVisitRepository

    @InjectMockKs
    private lateinit var entryService: DefaultEntryService

    @Test
    fun `When ticket-code is already in use, then throws InvalidTicketCodeException`() {
        val ticketCodeInUse = 9876L
        every {
            ongoingVisitRepository.existsByTicketCode(ticketCodeInUse)
        } returns true

        assertThatExceptionOfType(InvalidTicketCodeException::class.java)
            .isThrownBy { entryService.addVisit(ticketCodeInUse) }
            .withMessage("The ticket code provided ($ticketCodeInUse) is already in use")
    }

    @Test
    fun `When ticket-code is not in use, then saves new entry`() {
        val ticketCode = 1234L
        every {
            ongoingVisitRepository.existsByTicketCode(ticketCode)
        } returns false

        every {
            ongoingVisitRepository.save(any())
        } returns OngoingVisit()

        entryService.addVisit(1234)

        verify { ongoingVisitRepository.save(match { it.ticketCode == ticketCode }) }
    }
}