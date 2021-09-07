package com.johngachihi.parking.web.parkingTariffSettings

import com.johngachihi.parking.entities.ParkingTariff
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class OverwriteParkingTariffSettingsDto(
    @get:Valid
    @get:NotNull(message = "Parking tariff settings is required")
    @UniqueUpperLimit
    val newParkingTariffSettings: List<ParkingTariff>?
)