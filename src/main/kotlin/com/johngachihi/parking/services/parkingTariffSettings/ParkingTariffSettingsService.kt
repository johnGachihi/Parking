package com.johngachihi.parking.services.parkingTariffSettings

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.repositories.ParkingTariffSettingsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ParkingTariffSettingsService {
    fun getParkingTariffSettings(): List<ParkingTariff>
    fun overwriteParkingTariffSettings(newParkingTariffSettings: List<ParkingTariff>)
}

@Service
class DefaultParkingTariffSettingsService(
    @Autowired private val parkingTariffSettingsRepository: ParkingTariffSettingsRepository
) : ParkingTariffSettingsService {
    override fun getParkingTariffSettings(): List<ParkingTariff> {
        return parkingTariffSettingsRepository.findAllOrderedByUpperLimit()
    }

    @Transactional
    override fun overwriteParkingTariffSettings(newParkingTariffSettings: List<ParkingTariff>) {
        parkingTariffSettingsRepository.deleteAll()
        parkingTariffSettingsRepository.saveAll(newParkingTariffSettings)
    }

}