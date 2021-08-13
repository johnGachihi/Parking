package com.johngachihi.parking.modbustcp.camel

import com.digitalpetri.modbus.FunctionCode
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse
import com.johngachihi.parking.modbustcp.IllegalAddressException
import com.johngachihi.parking.modbustcp.UnsupportedFunctionException
import org.apache.camel.Predicate
import org.apache.camel.builder.PredicateBuilder
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

// TODO: Create ExceptionHandler component for mapping any exception
//       to a response (ModbusTcpPayload)

@Component
class ModbusTcpRoute(
    @Autowired
    private val modbusProps: ModbusTcpEndpointProperties,
) : RouteBuilder() {
    override fun configure() {
        val nettyUrl = "netty:tcp://${modbusProps.address}:${modbusProps.port}?" +
                "decoders=#modbusTcpMasterCodecFactory"

        from(nettyUrl).routeId("modbusEndpoint")
            .choice()
                .`when`(isModbusFunctionCode(FunctionCode.WriteMultipleRegisters))
                    .to("direct:writeRequest")
                .otherwise()
                    .throwException(UnsupportedFunctionException()).id("throwUnsupportedFunctionException")

        from("direct:writeRequest").routeId("writeRequest")
            .choice()
                .`when`(isWriteRequestAddress(1))
                    .bean("entryRequestHandler").id("entryRequestHandler")
                .`when`(isWriteRequestAddress(2))
                    .bean("exitRequestHandler").id("exitRequestHandler")
                .otherwise()
                    .throwException(IllegalAddressException())
    }

    private fun isModbusFunctionCode(functionCode: FunctionCode): Predicate {
        return PredicateBuilder.toPredicate(
            simple("\${body.modbusPdu.functionCode.code} == ${functionCode.code}")
        )
    }

    private fun isWriteRequestAddress(address: Int): Predicate {
        return PredicateBuilder.toPredicate(
            simple("\${body.modbusPdu.address} == $address")
        )
    }
}

@Component
class WriteRequestResponseComponent {
    fun a(body: ModbusTcpPayload): ModbusTcpPayload {
        val requestPdu = body.modbusPdu as WriteMultipleRegistersRequest

        val modbusPdu = WriteMultipleRegistersResponse(
            requestPdu.address, requestPdu.quantity
        )
        return ModbusTcpPayload(body.transactionId, body.unitId, modbusPdu)
    }
}