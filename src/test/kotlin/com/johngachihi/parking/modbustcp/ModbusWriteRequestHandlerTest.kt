package com.johngachihi.parking.modbustcp

import com.johngachihi.parking.modbustcp.controllers.ModbusController
import com.johngachihi.parking.modbustcp.decoders.Decoder
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseFactory
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus
import com.johngachihi.parking.modbustcp.requestHandling.ModbusWriteRequestHandler
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.bind.annotation.ResponseStatus

@DisplayName("Test ModbusWriteRequestHandler")
@ExtendWith(MockKExtension::class)
internal class ModbusWriteRequestHandlerTest {
    @RelaxedMockK
    private lateinit var decoder: Decoder<Long>

    @RelaxedMockK
    private lateinit var modbusController: ModbusController<Long>

    @RelaxedMockK
    private lateinit var modbusResponseFactory: ModbusResponseFactory

    @InjectMockKs
    private lateinit var modbusWriteRequestHandler: ModbusWriteRequestHandler<Long>

    @Test
    fun `When Modbus payload passed is not for a WriteMultipleRegister request, then throws an AssertException`() {
        val modbusPayload = makeReadCoilModbusRequestMessage()

        assertThatExceptionOfType(AssertionError::class.java)
            .isThrownBy {
                modbusWriteRequestHandler.handle(modbusPayload)
            }
            .withMessage(
                "The Modbus message handled by " +
                        "${ModbusWriteRequestHandler::class.qualifiedName} " +
                        "must have a WriteMultipleRegisterRequest PDU."
            )

    }

    @Test
    fun `Uses decoder to decode the write-data from the write-multiple-register request`() {
        val writeData = Unpooled.buffer().writeInt(12)
        val modbusPayload = makeWriteMultipleRegisterModbusRequestMessage(writeData = writeData)

        modbusWriteRequestHandler.handle(modbusPayload)

        verify { decoder.decode(writeData) }
    }

    @Test
    fun `Passes decoded data to the ModbusController to handle the request (run business logic)`() {
        every { decoder.decode(any()) } returns 12

        val modbusPayload = makeWriteMultipleRegisterModbusRequestMessage()

        modbusWriteRequestHandler.handle(modbusPayload)

        verify { modbusController.handleRequest(12) }
    }

    @Test
    fun `Passes ResponseStatus from the ModbusController to the ModbusWriteRequestResponseFactory`() {
        every {
            modbusController.handleRequest(any())
        } returns ModbusResponseStatus.ILLEGAL_DATA

        val modbusPayload = makeWriteMultipleRegisterModbusRequestMessage()
        modbusWriteRequestHandler.handle(modbusPayload)

        verify {
            modbusResponseFactory.createResponse(
                modbusPayload,
                ModbusResponseStatus.ILLEGAL_DATA
            )
        }
    }

    @Test
    fun `Returns response as created by the Modbus exchange`() {
        val expectedResponsePayload = makeWriteMultipleRegisterModbusResponseMessage()
        every { modbusResponseFactory.createResponse(any(), any()) } returns expectedResponsePayload

        val modbusPayload = makeWriteMultipleRegisterModbusRequestMessage()
        val actualResponsePayload = modbusWriteRequestHandler.handle(modbusPayload)

        assertThat(actualResponsePayload).isEqualTo(expectedResponsePayload)
    }
}