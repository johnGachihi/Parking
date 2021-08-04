package com.johngachihi.parking.modbustcp

import com.digitalpetri.modbus.ModbusPdu
import com.digitalpetri.modbus.codec.ModbusRequestEncoder
import com.digitalpetri.modbus.codec.ModbusResponseDecoder
import com.digitalpetri.modbus.codec.ModbusTcpCodec
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.requests.ReadCoilsRequest
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
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


fun makeModbusMessage(
    transactionId: Short = 1,
    unitId: Short = 1,
    pdu: ModbusPdu
): ModbusTcpPayload {
    return ModbusTcpPayload(transactionId, unitId, pdu)
}

fun makeWriteMultipleRegisterRequestPdu(
    address: Int,
    quantity: Int,
    writeData: ByteBuf = Unpooled.buffer().writeInt(12)
) = WriteMultipleRegistersRequest(address, quantity, writeData)

fun makeWriteMultipleRegisterModbusRequestMessage(
    transactionId: Short = 1,
    unitId: Short = 1,
    address: Int = 1,
    quantity: Int = 1,
    writeData: ByteBuf = Unpooled.buffer().writeInt(12)
): ModbusTcpPayload =
    makeModbusMessage(
        transactionId = transactionId,
        unitId = unitId,
        pdu = WriteMultipleRegistersRequest(address, quantity, writeData)
    )

fun makeReadCoilModbusRequestMessage() =
    makeModbusMessage(pdu = ReadCoilsRequest(1, 1))

fun makeWriteMultipleRegisterModbusResponseMessage() =
    makeModbusMessage(pdu = WriteMultipleRegistersResponse(1, 1))