package com.johngachihi.parking.modbustcp

import io.netty.buffer.ByteBuf

interface Decoder<T> {
    fun decode(data: ByteBuf): T
}