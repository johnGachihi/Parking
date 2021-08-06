package com.johngachihi.parking.modbustcp.decoders

import com.johngachihi.parking.modbustcp.decoders.Decoder
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Component

@Component
class RfidDecoder : Decoder<Long> {
    override fun decode(data: ByteBuf): Long {
        if (data.readableBytes() != 8) {
            throw DecodingException(
                "Invalid Rfid code provided. The Rfid must be 8-bytes long"
            )
        }

        return data.readLongLE()
    }
}