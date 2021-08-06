package com.johngachihi.parking.modbustcp.requestHandling

import com.digitalpetri.modbus.codec.ModbusTcpPayload

interface ModbusExchange {
    fun createResponse(request: ModbusTcpPayload): ModbusTcpPayload
}

