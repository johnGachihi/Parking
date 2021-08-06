package com.johngachihi.parking.modbustcp.camel

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "modbus-tcp.endpoint")
@ConstructorBinding
data class ModbusTcpEndpointProperties(val port: Int?, val address: String?)