package com.johngachihi.parking.modbustcp.controllers

import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus
import com.johngachihi.parking.services.EntryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ModbusEntryController(
    @Autowired
    private val entryService: EntryService
) : ModbusController<Long> {
    override fun handleRequest(msg: Long) =
        try {
            entryService.addVisit(msg)
            ModbusResponseStatus.OK
        } catch (e: InvalidTicketCodeException) {
            ModbusResponseStatus.ILLEGAL_DATA
        }
}