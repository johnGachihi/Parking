package com.johngachihi.parking.services.parkingTariffSettings

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.minutes
import com.johngachihi.parking.repositories.ParkingTariffSettingsRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.validation.ConstraintViolationException

@DisplayName("Test DefaultParkingTariffSettingsService")
@ExtendWith(MockKExtension::class)
internal class DefaultParkingTariffSettingsServiceTest {
    @RelaxedMockK
    private lateinit var parkingTariffSettingsRepository: ParkingTariffSettingsRepository

    @InjectMockKs
    private lateinit var parkingTariffSettingsService: DefaultParkingTariffSettingsService

    @Nested
    @DisplayName("Test getParkingTariffSettings()")
    inner class TestGetParkingTariffSettingsMethod {
        @Test
        fun `Returns result from ParkingTariffSettingsRepository#findAllOrderedByUpperLimit`() {
            val expectedResult = listOf(
                ParkingTariff().apply { upperLimit = 10.minutes },
                ParkingTariff().apply { upperLimit = 20.minutes }
            )

            every {
                parkingTariffSettingsRepository.findAllOrderedByUpperLimit()
            } returns expectedResult

            val actualResult = parkingTariffSettingsService.getParkingTariffSettings()

            assertThat(actualResult).isEqualTo(expectedResult)

        }
    }

    @Nested
    @DisplayName("Test overwriteParkingTariffSettings()")
    inner class TestOverwriteParkingTariffSettingsMethod {
        private val parkingTariffData = listOf(
            ParkingTariff().apply { upperLimit = 10.minutes; fee = 10.0 },
            ParkingTariff().apply { upperLimit = 20.minutes; fee = 20.0 }
        )

        @Test
        @DisplayName(
            "When parking tariff settings contains ParkingTariff with similar " +
                    "(not unique) upperLimit, then throws IllegalArgumentException"
        )
        fun testWhenParkingTariffSettingsContainsParkingTariffsWithSimilarUpperLimit() {
            val parkingTariffData = listOf(
                ParkingTariff().apply { upperLimit = 10.minutes; fee = 10.0 },
                ParkingTariff().apply { upperLimit = 10.minutes; fee = 10.0 }
            )

            assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy {
                    parkingTariffSettingsService.overwriteParkingTariffSettings(parkingTariffData)
                }
                .withMessage("The upperLimits for the parking-tariff settings should be unique")
        }

        @Test
        fun `Deletes current parking tariff data then inserts new parking tariff data`() {
            parkingTariffSettingsService.overwriteParkingTariffSettings(parkingTariffData)

            verifyOrder {
                parkingTariffSettingsRepository.deleteAll()
                parkingTariffSettingsRepository.saveAll(parkingTariffData)
            }
        }
    }
}