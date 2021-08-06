package com.johngachihi.parking.modbustcp.camel

import com.digitalpetri.modbus.codec.ModbusRequestDecoder
import com.digitalpetri.modbus.codec.ModbusResponseEncoder
import com.digitalpetri.modbus.codec.ModbusTcpCodec
import io.netty.channel.ChannelHandler
import org.apache.camel.component.netty.DefaultChannelHandlerFactory
import org.springframework.stereotype.Component

@Component
class ModbusTcpMasterCodecFactory : DefaultChannelHandlerFactory() {
    override fun newChannelHandler(): ChannelHandler {
        return ModbusTcpCodec(ModbusResponseEncoder(), ModbusRequestDecoder())
    }
}