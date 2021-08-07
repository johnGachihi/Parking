package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.ExceptionCode
import com.digitalpetri.modbus.FunctionCode
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.*
import com.digitalpetri.modbus.responses.ExceptionResponse
import com.johngachihi.parking.ParkingApplication
import com.johngachihi.parking.modbustcp.camel.ModbusTcpEndpointProperties
import io.netty.buffer.Unpooled
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.AdviceWith
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.model.BeanDefinition
import org.apache.camel.model.ThrowExceptionDefinition
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.apache.camel.test.spring.junit5.UseAdviceWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@CamelSpringBootTest
@SpringBootTest(classes = [ParkingApplication::class])
@UseAdviceWith
internal class ModbusTcpRouteTest {
    @Autowired
    private lateinit var producerTemplate: ProducerTemplate

    @Autowired
    private lateinit var camelContext: CamelContext

    @Test
    fun a() {
        camelContext.start()

        camelContext.endpoints.forEach {
            println(it.endpointUri)
        }
    }

    @Nested
    @DisplayName("When the incoming message is a WriteMultipleRegister request")
    inner class TestWhenMessageIsAWriteMultipleRegistersRequest {
        @BeforeEach
        fun init() {
            AdviceWith.adviceWith(camelContext, "modbusEndpoint") {
                it.replaceFromWith("direct:start")
            }
        }

        @Test
        fun `then it is routed to the writeRequest route`() {
            AdviceWith.adviceWith(camelContext, "writeRequest") {
                it.interceptFrom().to("mock:writeRequest").stop()
            }
            camelContext.start()

            val writeRequestRouteMock = camelContext.getEndpoint("mock:writeRequest") as MockEndpoint
            writeRequestRouteMock.expectedMessageCount(1)

            val modbusMessage = makeWriteMultipleRegisterModbusRequestMessage()
            producerTemplate.sendBody("direct:start", modbusMessage)

            writeRequestRouteMock.assertIsSatisfied()
        }

        @Test
        @DisplayName(
            "And its WriteMultipleRegister's address field is 2, " +
                    "then message is routed to the exitRequestHandler bean"
        )
        fun testAddressFieldIs2() {
            AdviceWith.adviceWith(camelContext, "writeRequest") {
                it.weaveById<BeanDefinition>("exitRequestHandler")
                    .replace().to("mock:exitRequestHandler").stop()
            }

            camelContext.start()

            val exitRequestHandlerMock = camelContext.getEndpoint(
                "mock:exitRequestHandler"
            ) as MockEndpoint
            exitRequestHandlerMock.expectedMessageCount(1)

            val modbusMessage = makeWriteMultipleRegisterModbusRequestMessage(address = 2)
            producerTemplate.sendBody("direct:start", modbusMessage)

            exitRequestHandlerMock.assertIsSatisfied()
        }

        @Test
        @DisplayName(
            "And its WriteMultipleRegister's address field is not supported, " +
                    "then it is routed to the throw-UnsupportedAddressException endpoint"
        )
        fun testAddressFieldUnsupported() {
            AdviceWith.adviceWith(camelContext, "writeRequest") {
                it.weaveByType(ThrowExceptionDefinition::class.java)
                    .replace().to("mock:throwUnsupportedAddressException").stop()
            }

            camelContext.start()

            val throwExceptionEndpointMock = camelContext.getEndpoint(
                "mock:throwUnsupportedAddressException"
            ) as MockEndpoint
            throwExceptionEndpointMock.expectedMessageCount(1)

            val modbusMessage = makeWriteMultipleRegisterModbusRequestMessage(address = -1)
            producerTemplate.sendBody("direct:start", modbusMessage)

            throwExceptionEndpointMock.assertIsSatisfied()
        }
    }


    @Test
    fun `When incoming message is for unsupported function codes, hits the throwUnsupportedFunctionException endpoint`() {
        AdviceWith.adviceWith(camelContext, "modbusEndpoint") {
            it.replaceFromWith("direct:start")
            it.weaveById<ThrowExceptionDefinition>("throwUnsupportedFunctionException")
                .replace().to("mock:throwUnsupportedFunctionException")
        }

        camelContext.start()

        val mockEndpoint = camelContext.getEndpoint(
            "mock:throwUnsupportedFunctionException"
        ) as MockEndpoint

        val unsupportedFunctionCode = listOf(
            FunctionCode.ReadCoils,
            FunctionCode.ReadDiscreteInputs,
            FunctionCode.ReadHoldingRegisters,
            FunctionCode.ReadInputRegisters,
            FunctionCode.WriteSingleCoil,
            FunctionCode.WriteMultipleCoils,
            FunctionCode.MaskWriteRegister,
            FunctionCode.ReadWriteMultipleRegisters
        )

        unsupportedFunctionCode.forEach {
            mockEndpoint.expectedMessageCount(1)

            producerTemplate.requestBody(
                "direct:start", makeModbusMessage(it)
            )
            mockEndpoint.assertIsSatisfied()
            mockEndpoint.reset()
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