package com.johngachihi.parking.services.parkingTariffSettings

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.minutes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class UtilKtTest {
    @Nested
    @DisplayName("Test isUpperLimitUnique()")
    inner class TestIsUpperLimitUniqueFunction {
        @Test
        fun `When upperLimits are unique, then returns true`() {
            val parkingTariffSettings = listOf(
                ParkingTariff().apply { upperLimit = 10.minutes },
                ParkingTariff().apply { upperLimit = 20.minutes },
                ParkingTariff().apply { upperLimit = 30.minutes },
            )

            assertThat(isUpperLimitUnique(parkingTariffSettings))
                .isTrue()
        }

        @Test
        fun `When upperLimits are not all unique, then returns false`() {
            val parkingTariffSettings = listOf(
                ParkingTariff().apply { upperLimit = 10.minutes },
                ParkingTariff().apply { upperLimit = 20.minutes },
                ParkingTariff().apply { upperLimit = 10.minutes },
            )

            assertThat(isUpperLimitUnique(parkingTariffSettings))
                .isFalse()
        }
    }
}