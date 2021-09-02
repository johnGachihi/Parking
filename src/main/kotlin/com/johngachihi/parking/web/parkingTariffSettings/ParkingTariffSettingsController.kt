package com.johngachihi.parking.web.parkingTariffSettings

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.services.parkingTariffSettings.ParkingTariffSettingsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/settings/parking-tariff")
class ParkingTariffSettingsController(
    @Autowired private val parkingTariffSettingsService: ParkingTariffSettingsService
) {
    @GetMapping
    fun getParkingTariffSettings(): List<ParkingTariff> {
        return parkingTariffSettingsService.getParkingTariffSettings()
    }

    @PutMapping
    fun overwriteParkingTariffSettings(
        @Valid @RequestBody
        overwriteParkingTariffSettingsDto: OverwriteParkingTariffSettingsDto
    ) {
        parkingTariffSettingsService.overwriteParkingTariffSettings(
            overwriteParkingTariffSettingsDto.newParkingTariffSettings!!
        )
    }
}