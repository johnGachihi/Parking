package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.ExceptionCode
import com.digitalpetri.modbus.responses.ExceptionResponse
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus
import com.johngachihi.parking.modbustcp.requestHandling.ModbusWriteRequestResponseFactory
import com.johngachihi.parking.modbustcp.requestHandling.ModbusWriteRequestHandler
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ModbusWriteRequestResponseFactoryTest {
    private lateinit var modbusWriteRequestResponseFactory: ModbusWriteRequestResponseFactory

    @BeforeEach
    fun initSUT() {
        modbusWriteRequestResponseFactory = ModbusWriteRequestResponseFactory()
    }

    @Test
    @DisplayName(
        "When the PDU of the request passed to createResponse" +
                "is not a WriteMultipleRegisterRequest, then throws an AssertionError"
    )
    fun testWhenPDUInvalidThrowsAssertionError() {
        val modbusMessage = makeReadCoilModbusRequestMessage()

        assertThatExceptionOfType(AssertionError::class.java)
            .isThrownBy {
                modbusWriteRequestResponseFactory.createResponse(modbusMessage)
            }
            .withMessage(
                "The Modbus message passed as a request to " +
                        "${ModbusWriteRequestHandler::class.qualifiedName}'s createResponse()" +
                        "must have a WriteMultipleRegisterRequest PDU.)"
            )
    }

    @Nested
    @DisplayName("When ResponseStatus is OK")
    inner class TestWhenResponseStatusIsOK {
        @Test
        fun `createResponse() maps the request's Modbus payload fields into the response appropriately`() {
            val expectedTransactionId: Short = 12
            val expectedUnitId: Short = 1

            val modbusMessage = makeWriteMultipleRegisterModbusRequestMessage(
                transactionId = expectedTransactionId,
                unitId = expectedUnitId,
            )

            val response = modbusWriteRequestResponseFactory.createResponse(modbusMessage)

            assertThat(response.transactionId).isEqualTo(expectedTransactionId)
            assertThat(response.unitId).isEqualTo(expectedUnitId)
        }

        @Test
        fun `createResponse() returns a response with a PDU which is an instance of WriteMultipleRegisterRequest`() {
            val modbusMessage = makeWriteMultipleRegisterModbusRequestMessage()
            val response = modbusWriteRequestResponseFactory.createResponse(modbusMessage)

            assertThat(response.modbusPdu)
                .isInstanceOf(WriteMultipleRegistersResponse::class.java)
        }

        @Test
        fun `createResponse() maps request's PDU fields into the response's appropriately`() {
            val expectedAddress = 1234
            val expectedQuantity = 9876

            val modbusMessage = makeModbusMessage(
                pdu = makeWriteMultipleRegisterRequestPdu(expectedAddress, expectedQuantity)
            )
            val response = modbusWriteRequestResponseFactory.createResponse(modbusMessage)

            val responsePdu = response.modbusPdu as WriteMultipleRegistersResponse
            assertThat(responsePdu.address).isEqualTo(expectedAddress)
            assertThat(responsePdu.quantity).isEqualTo(expectedQuantity)
        }
    }

    @Nested
    @DisplayName("When ResponseStatus is ILLEGAL_DATA")
    inner class TestWhenResponseStatusIsIllegalData {
        @Test
        fun `createResponse() returns a response with a PDU which is an instance of ExceptionResponse`() {
            val modbusMessage = makeWriteMultipleRegisterModbusRequestMessage()
            val response = modbusWriteRequestResponseFactory.createResponse(
                modbusMessage, ModbusResponseStatus.ILLEGAL_DATA
            )

            assertThat(response.modbusPdu)
                .isInstanceOf(ExceptionResponse::class.java)
        }

        @Test
        fun `createResponse() returns a response with an ExceptionCode_IllegalDataValue`() {
            val modbusMessage = makeWriteMultipleRegisterModbusRequestMessage()
            val response = modbusWriteRequestResponseFactory.createResponse(
                modbusMessage, ModbusResponseStatus.ILLEGAL_DATA
            )

            val responsePdu = response.modbusPdu as ExceptionResponse
            assertThat(responsePdu.exceptionCode)
                .isEqualTo(ExceptionCode.IllegalDataValue)
        }
    }
}