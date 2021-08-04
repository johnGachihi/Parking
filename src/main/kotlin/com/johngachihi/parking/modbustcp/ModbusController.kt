package com.johngachihi.parking.modbustcp

interface ModbusController<T, R> {
    fun handleRequest(msg: T): R
}