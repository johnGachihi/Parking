package com.johngachihi.parking

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.time.Instant
import java.time.temporal.ChronoUnit

val Int.minutesAgo: Instant
    get() = Instant.now().minus(this.toLong(), ChronoUnit.MINUTES)

fun make8Byte_ByteBufFromLong_LE(l: Long): ByteBuf =
    Unpooled.buffer().writeLongLE(l)