package com.johngachihi.parking.modbustcp.requestHandling

import com.digitalpetri.modbus.ExceptionCode
import com.digitalpetri.modbus.FunctionCode
import com.digitalpetri.modbus.ModbusPdu
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.digitalpetri.modbus.responses.ExceptionResponse
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse

class ModbusWriteRequestResponseFactory : ModbusResponseFactory {
    override fun createResponse(request: ModbusTcpPayload, status: ModbusResponseStatus): ModbusTcpPayload {
        assert(request.modbusPdu is WriteMultipleRegistersRequest) {
            "The Modbus message passed as a request to " +
                    "${ModbusWriteRequestHandler::class.qualifiedName}'s createResponse()" +
                    "must have a WriteMultipleRegisterRequest PDU.)"
        }
        val requestPdu = request.modbusPdu as WriteMultipleRegistersRequest

        val responsePdu = when (status) {
            ModbusResponseStatus.OK -> createResponsePduForStatusOK(requestPdu)
            ModbusResponseStatus.ILLEGAL_DATA -> createResponsePduForStatusIllegalData()
        }

        return ModbusTcpPayload(
            request.transactionId,
            request.unitId,
            responsePdu
        )
    }

    private fun createResponsePduForStatusOK(requestPdu: WriteMultipleRegistersRequest): ModbusPdu {
        return WriteMultipleRegistersResponse(requestPdu.address, requestPdu.quantity)
    }

    private fun createResponsePduForStatusIllegalData() =
        ExceptionResponse(FunctionCode.WriteMultipleRegisters, ExceptionCode.IllegalDataValue)
}