package com.johngachihi.parking.modbustcp.controllers

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.UnpaidFeeException
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus
import com.johngachihi.parking.services.ExitService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Test ModbusExitController")
@ExtendWith(MockKExtension::class)
internal class ModbusExitControllerTest {
    @RelaxedMockK
    private lateinit var exitService: ExitService

    @InjectMockKs
    private lateinit var modbusExitController: ModbusExitController

    @Test
    fun `Uses ExitService to complete a visit`() {
        modbusExitController.handleRequest(987)

        verify { exitService.finishVisit(987) }
    }

    @Nested
    @DisplayName("When ExitService#finishVisit throws...")
    inner class TestWhenExitServiceThrows {
        @Test
        fun `InvalidTicketCodeException, then returns response-status ILLEGAL_DATA`() {
            every {
                exitService.finishVisit(any())
            } throws InvalidTicketCodeException("")

            val responseStatus = modbusExitController.handleRequest(123)

            assertThat(responseStatus)
                .isEqualTo(ModbusResponseStatus.ILLEGAL_DATA)
        }

        @Test
        fun `UnpaidFeeException, then returns response-status ILLEGAL_DATA`() {
            every {
                exitService.finishVisit(any())
            } throws UnpaidFeeException("")

            val responseStatus = modbusExitController.handleRequest(123)

            assertThat(responseStatus)
                .isEqualTo(ModbusResponseStatus.ILLEGAL_DATA)
        }
    }
}