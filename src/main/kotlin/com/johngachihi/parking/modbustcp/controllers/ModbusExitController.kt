package com.johngachihi.parking.modbustcp.controllers

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.UnpaidFeeException
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus
import com.johngachihi.parking.services.ExitService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ModbusExitController(
    @Autowired
    private val exitService: ExitService
) : ModbusController<Long> {
    override fun handleRequest(msg: Long): ModbusResponseStatus {
        try {
            exitService.finishVisit(msg)
        } catch (e: InvalidTicketCodeException) {
            return ModbusResponseStatus.ILLEGAL_DATA
        } catch (e: UnpaidFeeException) {
            return ModbusResponseStatus.ILLEGAL_DATA
        }

        return ModbusResponseStatus.OK
    }
}