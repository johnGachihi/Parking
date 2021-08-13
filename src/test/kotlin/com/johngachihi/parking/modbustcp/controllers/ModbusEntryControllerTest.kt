package com.johngachihi.parking.modbustcp.controllers

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus
import com.johngachihi.parking.services.EntryService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Test ModbusEntryController")
@ExtendWith(MockKExtension::class)
internal class ModbusEntryControllerTest {
    @MockK
    private lateinit var entryService: EntryService

    @InjectMockKs
    private lateinit var modbusEntryController: ModbusEntryController

    @Test
    fun `Uses EntryService to add a new visit`() {
        every { entryService.addVisit(any()) } answers {}

        modbusEntryController.handleRequest(1998)

        verify { entryService.addVisit(1998) }
    }

    @Test
    @DisplayName(
        "When attempt to add new entry throws an InvalidTicketCodeException, " +
                "then returns an IllegalData response status"
    )
    fun testWhenInvalidTicketCodeExceptionThrown() {
        every { entryService.addVisit(any()) } throws InvalidTicketCodeException("")

        val responseStatus = modbusEntryController.handleRequest(1234)

        assertThat(responseStatus)
            .isEqualTo(ModbusResponseStatus.ILLEGAL_DATA)
    }
}