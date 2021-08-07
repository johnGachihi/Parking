package com.johngachihi.parking.modbustcp.controllers

import com.johngachihi.parking.ExitService
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus

@Component
class ModbusExitController : ModbusController<Long> {
    override fun handleRequest(msg: Long): ModbusResponseStatus {
        println("Not yet implemented")
        return ModbusResponseStatus.OK
    }
}