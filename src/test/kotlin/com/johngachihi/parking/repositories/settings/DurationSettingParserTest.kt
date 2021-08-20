package com.johngachihi.parking.repositories.settings

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration

@DisplayName("Test DurationSettingParser")
internal class DurationSettingParserTest {
    private lateinit var durationSettingParser: DurationSettingParser

    @BeforeEach
    fun init() {
        durationSettingParser = DurationSettingParser()
    }

    @Nested
    @DisplayName("Test parseMinutesString")
    inner class TestParseMinutesString {
        @Nested
        @DisplayName("When the provided minutesString argument is invalid because it is")
        inner class TestWhenMinutesStringArgumentIsInvalid {
            @Test
            fun `Not a number, then throws NumberFormatException`() {
                assertThatExceptionOfType(NumberFormatException::class.java)
                    .isThrownBy { durationSettingParser.parseMinutesString("abc") }
            }

            @Test
            fun `Below zero, then throws IllegalFormatException with appropriate message`() {
                val minutesString = "-1"
                assertThatExceptionOfType(IllegalFormatException::class.java)
                    .isThrownBy { durationSettingParser.parseMinutesString(minutesString) }
                    .withMessage("The value provided is less than 0: $minutesString")
            }

            @Test
            fun `Above the specified max, then throws IllegalArgumentException with appropriate message`() {
                val minutesString = "20"
                assertThatExceptionOfType(IllegalFormatException::class.java)
                    .isThrownBy {
                        durationSettingParser.parseMinutesString(
                            minutesString = minutesString,
                            maxDuration = Duration.ofMinutes(10)
                        )
                    }
                    .withMessage(
                        "The value provided ($minutesString) is greater than the specified maximum " +
                                "${Duration.ofMinutes(10).toMinutes()}"
                    )
            }
        }

        @Test
        fun `When the provided minutesString argument is valid, then returns correctly parsed Duration`() {
            val duration = durationSettingParser.parseMinutesString("10")

            assertThat(duration)
                .isEqualTo(Duration.ofMinutes(10))
        }
    }
}