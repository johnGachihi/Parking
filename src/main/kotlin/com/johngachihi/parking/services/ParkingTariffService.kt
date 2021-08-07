package com.johngachihi.parking.services

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.repositories.ParkingTariffRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration

interface ParkingTariffService {
    fun getFee(duration: Duration): Double
}

@Service
class DefaultParkingTariffService(
    @Autowired
    private val parkingTariffRepository: ParkingTariffRepository
) : ParkingTariffService {
    private val orderedParkingTariffs: List<ParkingTariff> by lazy {
        parkingTariffRepository.getAllInAscOrderOfUpperLimit()
    }

    override fun getFee(duration: Duration): Double {
        return getOverlappingTariff(duration)?.fee ?: getHighestTariff()?.fee ?: 0.0
    }

    private fun getOverlappingTariff(duration: Duration) =
        orderedParkingTariffs.find { it.upperLimit > duration }

    private fun getHighestTariff() =
        parkingTariffRepository.getAllInAscOrderOfUpperLimit().lastOrNull()
}