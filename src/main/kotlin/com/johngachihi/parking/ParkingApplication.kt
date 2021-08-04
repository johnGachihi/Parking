package com.johngachihi.parking

import com.johngachihi.parking.modbustcp.ModbusTcpEndpointProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ModbusTcpEndpointProperties::class)
class ParkingApplication

fun main(args: Array<String>) {
    runApplication<ParkingApplication>(*args)
}