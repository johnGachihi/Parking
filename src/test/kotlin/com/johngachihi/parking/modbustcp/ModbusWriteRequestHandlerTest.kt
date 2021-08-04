package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.ModbusPdu
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.ReadCoilsRequest
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Test ModbusWriteRequestHandler")
@ExtendWith(MockKExtension::class)
internal class ModbusWriteRequestHandlerTest {
    @RelaxedMockK
    private lateinit var decoder: Decoder<Long>

    @RelaxedMockK
    private lateinit var modbusController: ModbusController<Long, Unit>

    @RelaxedMockK
    private lateinit var modbusExchange: ModbusExchange

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
    fun `Returns response as created by the Modbus exchange`() {
        val expectedResponsePayload = makeWriteMultipleRegisterModbusResponseMessage()
        every { modbusExchange.createResponse(any()) } returns expectedResponsePayload

        val modbusPayload = makeWriteMultipleRegisterModbusRequestMessage()
        val actualResponsePayload = modbusWriteRequestHandler.handle(modbusPayload)

        assertThat(actualResponsePayload).isEqualTo(expectedResponsePayload)
    }

    private fun makeModbusMessage(pdu: ModbusPdu): ModbusTcpPayload {
        return ModbusTcpPayload(1, 1, pdu)
    }

    private fun makeWriteMultipleRegisterModbusRequestMessage(
        writeData: ByteBuf = Unpooled.buffer().writeInt(12)
    ): ModbusTcpPayload {
        val pdu = WriteMultipleRegistersRequest(1, 1, writeData)
        return makeModbusMessage(pdu)
    }

    private fun makeReadCoilModbusRequestMessage() =
        makeModbusMessage(ReadCoilsRequest(1, 1))

    private fun makeWriteMultipleRegisterModbusResponseMessage() =
        makeModbusMessage(WriteMultipleRegistersResponse(1, 1))
}