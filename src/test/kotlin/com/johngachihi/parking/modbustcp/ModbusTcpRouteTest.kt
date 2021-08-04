package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.ExceptionCode
import com.digitalpetri.modbus.FunctionCode
import com.digitalpetri.modbus.ModbusPdu
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.*
import com.digitalpetri.modbus.responses.ExceptionResponse
import com.johngachihi.parking.ParkingApplication
import io.netty.buffer.Unpooled
import org.apache.camel.CamelContext
import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@CamelSpringBootTest
@SpringBootTest(classes = [ParkingApplication::class])
internal class ModbusTcpRouteTest {
    @Autowired
    private lateinit var modbusTcpEndpointProps: ModbusTcpEndpointProperties

    @Autowired
    private lateinit var producerTemplate: ProducerTemplate

    @Autowired
    private lateinit var camelContext: CamelContext

    // TODO: Rename
    private val nettyEndpointUrl: String by lazy {
        modbusTcpEndpointProps.let {
            "netty:tcp://${it.address}:${it.port}?" +
                    "requestTimeout=5000&" +
                    "encoders=#modbusTcpClientCodecFactory"
        }
    }

    @Test
    fun a() {
        camelContext.endpoints.forEach {
            println(it.endpointUri)
        }
    }

    @Test
    fun `When request is a WriteMultipleRegisters, then it is routed to mock foo`() {
        val writeMultipleRegisterMessage = makeModbusMessage(FunctionCode.WriteMultipleRegisters)

//        ...
    }

    @Nested
    @DisplayName("Unsupported Modbus function codes")
    inner class UnsupportedModbusFunctionCodes {
        private lateinit var response: Any

        @Test
        fun `test request with unsupported function codes, get back an IllegalFunction exception response`() {
            val unsupportedFunctionCode = listOf(
                FunctionCode.ReadCoils,
                FunctionCode.ReadDiscreteInputs,
                FunctionCode.ReadHoldingRegisters,
                FunctionCode.ReadInputRegisters,
                FunctionCode.WriteSingleCoil,
                FunctionCode.WriteMultipleCoils,
                FunctionCode.WriteMultipleRegisters,
                FunctionCode.MaskWriteRegister,
                FunctionCode.ReadWriteMultipleRegisters
            )

            for (functionCode in unsupportedFunctionCode) {
                response = producerTemplate.requestBody(
                    nettyEndpointUrl,
                    makeModbusMessage(functionCode)
                )
                assertResponseIsIllegalFunctionException()
            }
        }


        private fun assertResponseIsIllegalFunctionException() {
            assertThat(response).isInstanceOf(ModbusTcpPayload::class.java)
            val modbusResponse = response as ModbusTcpPayload

            assertThat(modbusResponse.modbusPdu).isInstanceOf(ExceptionResponse::class.java)
            val exceptionPdu = modbusResponse.modbusPdu as ExceptionResponse

            assertThat(exceptionPdu.exceptionCode).isEqualTo(ExceptionCode.IllegalFunction)
        }
    }

    private fun makeModbusMessage(functionCode: FunctionCode): ModbusTcpPayload {
        val pdu = when (functionCode) {
            FunctionCode.ReadCoils -> ReadCoilsRequest(1, 1)
            FunctionCode.ReadDiscreteInputs -> ReadDiscreteInputsRequest(1, 1)
            FunctionCode.ReadHoldingRegisters -> ReadHoldingRegistersRequest(1, 1)
            FunctionCode.ReadInputRegisters -> ReadInputRegistersRequest(1, 1)
            FunctionCode.WriteSingleCoil -> WriteSingleCoilRequest(1, true)
            FunctionCode.WriteSingleRegister -> WriteSingleRegisterRequest(1, 1)
            FunctionCode.WriteMultipleCoils -> WriteMultipleCoilsRequest(1, 8, Unpooled.buffer().writeByte(1))
            FunctionCode.WriteMultipleRegisters -> WriteMultipleRegistersRequest(
                1,
                1,
                Unpooled.buffer().writeShort(1)
            )
            FunctionCode.MaskWriteRegister -> MaskWriteRegisterRequest(1, 1, 1)
            FunctionCode.ReadWriteMultipleRegisters -> ReadWriteMultipleRegistersRequest(
                1,
                1,
                1,
                1,
                Unpooled.buffer().writeShort(1)
            )
            else -> throw IllegalArgumentException("Invalid function code (${functionCode.name}) provided.")
        }

        return ModbusTcpPayload(1, 1, pdu)
    }
}