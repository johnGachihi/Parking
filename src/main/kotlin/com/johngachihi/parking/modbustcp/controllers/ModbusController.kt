package com.johngachihi.parking.modbustcp.controllers

import com.johngachihi.parking.modbustcp.requestHandling.ModbusResponseStatus

interface ModbusController<T> {
    // TODO: Replace ModbusResponseStatus with a new ModbusResponse type
    //       This will make it more flexible especially if there is ever
    //       the need to to use it for ModbusReadRequests where the controller
    //       will be required to return a value along with the status.
    fun handleRequest(msg: T): ModbusResponseStatus
}