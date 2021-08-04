package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.ExceptionCode
import com.digitalpetri.modbus.FunctionCode
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.digitalpetri.modbus.responses.ExceptionResponse
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse
import io.netty.buffer.Unpooled
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.apache.camel.builder.PredicateBuilder
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.convert.ConverterBuilder
import org.springframework.stereotype.Component

@Component
class ModbusTcpRoute(
    @Autowired
    private val modbusProps: ModbusTcpEndpointProperties,
) : RouteBuilder() {
    override fun configure() {
        val nettyUrl = "netty:tcp://${modbusProps.address}:${modbusProps.port}?" +
                "decoders=#modbusTcpMasterCodecFactory"

        onException(UnsupportedFunctionException::class.java)
            .handled(true)
            .bean("unsupportedFunctionCodeExceptionHandler")

        from(nettyUrl)
            .choice()
                .`when`(isModbusFunctionCode(FunctionCode.WriteMultipleRegisters))
                    .to("bean:exitRequestHandler")
                .otherwise()
                    .throwException(UnsupportedFunctionException())
    }

    private fun isModbusFunctionCode(functionCode: FunctionCode): Predicate {
        return PredicateBuilder.toPredicate(
            simple("\${body.modbusPdu.functionCode.code} == ${functionCode.code}")
        )
    }
}

@Component
class MyComponent {
    fun a(body: ModbusTcpPayload): ModbusTcpPayload {
        val requestPdu = body.modbusPdu as WriteMultipleRegistersRequest

        val modbusPdu = WriteMultipleRegistersResponse(
            requestPdu.address, requestPdu.quantity
        )
        return ModbusTcpPayload(body.transactionId, body.unitId, modbusPdu)
    }
}

@Component
class UnsupportedFunctionCodeExceptionHandler {
    fun a(body: ModbusTcpPayload): ModbusTcpPayload {
        val exceptionalResponsePdu = ExceptionResponse(
            body.modbusPdu.functionCode,
            ExceptionCode.IllegalFunction
        )
        return ModbusTcpPayload(body.transactionId, body.unitId, exceptionalResponsePdu)
    }
}