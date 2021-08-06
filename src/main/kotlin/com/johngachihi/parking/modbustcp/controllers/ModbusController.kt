package com.johngachihi.parking.modbustcp.controllers

interface ModbusController<T, R> {
    fun handleRequest(msg: T): R
}