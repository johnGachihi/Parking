package com.johngachihi.parking.services

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.repositories.ParkingTariffRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@DisplayName("Test DefaultParkingTariff")
@ExtendWith(MockKExtension::class)
internal class DefaultParkingTariffServiceTest {
    @MockK
    private lateinit var parkingTariffRepository: ParkingTariffRepository

    @InjectMockKs
    private lateinit var parkingTariffService: DefaultParkingTariffService

    @Nested
    @DisplayName("When there is parking-tariff data")
    inner class TestWhenThereIsParkingTariffData {
        @BeforeEach
        fun init() {
            every {
                parkingTariffRepository.getAllInAscOrderOfUpperLimit()
            } returns listOf(
                makeParkingTariff(1, Duration.ofMinutes(10), fee = 1.0),
                makeParkingTariff(2, Duration.ofMinutes(20), fee = 2.0),
                makeParkingTariff(3, Duration.ofMinutes(30), fee = 3.0),
            )
        }

        @Test
        fun `and there is an overlapping tariff, then returns the overlapping tariff's fee`() {
            val fee = parkingTariffService.getFee(Duration.ofMinutes(19))

            assertThat(fee).isEqualTo(2.0)
        }

        @Test
        fun `and there is no overlapping tariff, returns the fee for the tariff with the highest upperLimit`() {
            val fee = parkingTariffService.getFee(Duration.ofMinutes(40))

            Assertions.assertEquals(3.0, fee)
        }

        private fun makeParkingTariff(id: Long, upperLimit: Duration, fee: Double = 1.0) =
            ParkingTariff().apply {
                this.id = id
                this.upperLimit = upperLimit
                this.fee = fee
            }
    }

    @Test
    fun `When there is no parking tariff data, returns 0`() {
        every {
            parkingTariffRepository.getAllInAscOrderOfUpperLimit()
        } returns emptyList()

        val fee = parkingTariffService.getFee(Duration.ofMinutes(40))

        assertThat(fee).isEqualTo(0.0)
    }
}