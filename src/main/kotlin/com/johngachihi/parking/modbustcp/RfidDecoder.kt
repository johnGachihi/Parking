package com.johngachihi.parking.modbustcp

import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Component

@Component
class RfidDecoder : Decoder<Long> {
    override fun decode(data: ByteBuf): Long {
        TODO("Not yet implemented")
    }
}