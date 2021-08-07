package com.johngachihi.parking.services

import com.johngachihi.parking.entities.OngoingVisit

interface PaymentService {
    fun calculateParkingFee(ongoingVisit: OngoingVisit): Double
}