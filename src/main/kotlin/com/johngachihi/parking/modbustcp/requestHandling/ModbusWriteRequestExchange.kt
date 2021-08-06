package com.johngachihi.parking.modbustcp.requestHandling

import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse

class ModbusWriteRequestExchange : ModbusExchange {
    override fun createResponse(request: ModbusTcpPayload): ModbusTcpPayload {
        assert(request.modbusPdu is WriteMultipleRegistersRequest) {
            "The Modbus message passed as a request to " +
                    "${ModbusWriteRequestHandler::class.qualifiedName}'s createResponse()" +
                    "must have a WriteMultipleRegisterRequest PDU.)"
        }
        val requestPdu = request.modbusPdu as WriteMultipleRegistersRequest

        val responsePdu = WriteMultipleRegistersResponse(requestPdu.address, requestPdu.quantity)
        return ModbusTcpPayload(
            request.transactionId,
            request.unitId,
            responsePdu
        )
    }
}