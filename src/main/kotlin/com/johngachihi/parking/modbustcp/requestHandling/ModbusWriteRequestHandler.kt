package com.johngachihi.parking.modbustcp.requestHandling

import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.johngachihi.parking.modbustcp.controllers.ModbusController
import com.johngachihi.parking.modbustcp.decoders.Decoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class ModbusWriteRequestHandler<T>(
    private val decoder: Decoder<T>,
    private val modbusController: ModbusController<T, Unit>,
    private val modbusExchange: ModbusExchange = ModbusWriteRequestExchange()
) {
    fun handle(body: ModbusTcpPayload): ModbusTcpPayload {
        assert(body.modbusPdu is WriteMultipleRegistersRequest) {
            "The Modbus message handled by ${this::class.qualifiedName} " +
                    "must have a WriteMultipleRegisterRequest PDU."
        }
        val pdu = body.modbusPdu as WriteMultipleRegistersRequest

        modbusController.handleRequest(
            decoder.decode(pdu.values))

        return modbusExchange.createResponse(body)
    }
}

@Configuration
class A {
    @Bean
    fun exitRequestHandler(
        rfidDecoder: Decoder<Long>,
        modbusExitController: ModbusController<Long, Unit>
    ): ModbusWriteRequestHandler<Long> {
        return ModbusWriteRequestHandler(rfidDecoder, modbusExitController)
    }
}
