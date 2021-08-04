package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.codec.ModbusTcpPayload

interface ModbusExchange {
    fun createResponse(request: ModbusTcpPayload): ModbusTcpPayload
}

