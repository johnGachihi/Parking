package com.johngachihi.parking.services

import java.time.Duration

interface ParkingTariffService {
    fun getFee(duration: Duration): Double
}