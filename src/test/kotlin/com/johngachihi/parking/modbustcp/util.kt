package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.codec.ModbusRequestEncoder
import com.digitalpetri.modbus.codec.ModbusResponseDecoder
import com.digitalpetri.modbus.codec.ModbusTcpCodec
import io.netty.channel.ChannelHandler
import org.apache.camel.component.netty.DefaultChannelHandlerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class ModbusTcpClientCodecFactory : DefaultChannelHandlerFactory() {
    override fun newChannelHandler(): ChannelHandler {
        return ModbusTcpCodec(ModbusRequestEncoder(), ModbusResponseDecoder())
    }
}