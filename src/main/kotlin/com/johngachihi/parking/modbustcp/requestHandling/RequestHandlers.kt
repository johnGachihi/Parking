package com.johngachihi.parking.modbustcp.requestHandling

import com.johngachihi.parking.modbustcp.controllers.ModbusController
import com.johngachihi.parking.modbustcp.decoders.Decoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RequestHandlers {
    @Bean
    fun exitRequestHandler(
        rfidDecoder: Decoder<Long>,
        modbusExitController: ModbusController<Long>
    ): ModbusWriteRequestHandler<Long> {
        return ModbusWriteRequestHandler(rfidDecoder, modbusExitController)
    }
}