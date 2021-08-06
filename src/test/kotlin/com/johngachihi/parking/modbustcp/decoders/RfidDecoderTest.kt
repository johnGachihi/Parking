package com.johngachihi.parking.modbustcp.decoders

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Test RfidDecoder")
internal class RfidDecoderTest {
    @Test
    fun `When data is not 8 bytes long, then throws DecoderException`() {
        assertThatExceptionOfType(DecodingException::class.java)
            .isThrownBy {
                RfidDecoder().decode(makeByteBuf(2))
            }
            .withMessage("Invalid Rfid code provided. The Rfid must be 8-bytes long")

        assertThatExceptionOfType(DecodingException::class.java)
            .isThrownBy {
                RfidDecoder().decode(makeByteBuf(9))
            }
    }

    @Test
    fun `When data is correctly formatted, then returns the Long from the ByteBuf content`() {
        val expectedLong = 12344L
        val actualLong = RfidDecoder().decode(make8Byte_ByteBufFromLong_LE(12344))

        assertThat(actualLong).isEqualTo(expectedLong)
    }

    private fun makeByteBuf(n: Int): ByteBuf {
        val data = Unpooled.buffer()
        for (i in 1..n) {
            data.writeByte(i)
        }
        return data
    }

    private fun make8Byte_ByteBufFromLong_LE(l: Long): ByteBuf =
        Unpooled.buffer().writeLongLE(l)
}