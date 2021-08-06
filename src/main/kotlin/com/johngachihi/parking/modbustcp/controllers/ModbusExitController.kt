package com.johngachihi.parking.modbustcp.controllers

import org.springframework.stereotype.Component

@Component
class ModbusExitController : ModbusController<Long, Unit> {
    override fun handleRequest(msg: Long) {
        TODO("Not yet implemented")
    }
}