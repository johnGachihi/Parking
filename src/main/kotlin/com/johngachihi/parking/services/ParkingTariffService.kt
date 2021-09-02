package com.johngachihi.parking.services

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.repositories.ParkingTariffRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

interface ParkingTariffService {
    fun getFee(duration: Duration): Double
}

@Service
class DefaultParkingTariffService(
    @Autowired
    private val parkingTariffRepository: ParkingTariffRepository
) : ParkingTariffService {

    // - When there is an overlapping tariff returns its fee
    // - When there is no overlapping tariff return the fee for the highest tariff,
    //   i.e, the tariff with the highest upperLimit
    // - When there is neither an overlapping tariff nor highest tariff (meaning
    //   that there are no tariffs) then default to 0.0
    @Transactional(isolation = Isolation.READ_COMMITTED)
    override fun getFee(duration: Duration): Double {
        val orderedParkingTariffs = parkingTariffRepository.getAllInAscOrderOfUpperLimit()

        val tariff = getOverlappingTariff(orderedParkingTariffs, duration)
            ?: getHighestTariff(orderedParkingTariffs)

        return tariff?.fee ?: 0.0
    }

    private fun getOverlappingTariff(orderedParkingTariffs: List<ParkingTariff>, duration: Duration) =
        orderedParkingTariffs.find { it.upperLimit > duration }

    private fun getHighestTariff(orderedParkingTariffs: List<ParkingTariff>) =
        orderedParkingTariffs.lastOrNull()
}