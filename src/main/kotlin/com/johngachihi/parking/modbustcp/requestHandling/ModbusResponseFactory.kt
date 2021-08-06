package com.johngachihi.parking.modbustcp.requestHandling

import com.digitalpetri.modbus.codec.ModbusTcpPayload

interface ModbusResponseFactory {
    fun createResponse(
        request: ModbusTcpPayload,
        status: ModbusResponseStatus = ModbusResponseStatus.OK
    ): ModbusTcpPayload
}

