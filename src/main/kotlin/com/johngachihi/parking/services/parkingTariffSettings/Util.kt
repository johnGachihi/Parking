package com.johngachihi.parking.services.parkingTariffSettings

import com.johngachihi.parking.entities.ParkingTariff

fun isUpperLimitUnique(parkingTariffSettings: List<ParkingTariff>): Boolean {
    val set = HashSet<Long>()
    parkingTariffSettings.forEach {
        val upperLimitLong = it.upperLimit.toMinutes()

        if (set.contains(upperLimitLong))
            return false

        set.add(upperLimitLong)
    }

    return true
}