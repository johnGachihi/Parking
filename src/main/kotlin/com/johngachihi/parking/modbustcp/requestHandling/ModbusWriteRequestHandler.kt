package com.johngachihi.parking.modbustcp.requestHandling

import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.johngachihi.parking.modbustcp.controllers.ModbusController
import com.johngachihi.parking.modbustcp.decoders.Decoder

class ModbusWriteRequestHandler<T>(
    private val decoder: Decoder<T>,
    private val modbusController: ModbusController<T>,
    private val modbusResponseFactory: ModbusResponseFactory = ModbusWriteRequestResponseFactory()
) {
    fun handle(body: ModbusTcpPayload): ModbusTcpPayload {
        assert(body.modbusPdu is WriteMultipleRegistersRequest) {
            "The Modbus message handled by ${this::class.qualifiedName} " +
                    "must have a WriteMultipleRegisterRequest PDU."
        }
        val pdu = body.modbusPdu as WriteMultipleRegistersRequest

        val responseStatus = modbusController.handleRequest(
            decoder.decode(pdu.values))

        return modbusResponseFactory.createResponse(body, responseStatus)
    }
}
