package com.johngachihi.parking.repositories

import java.time.Duration

interface ParkingFeeConfigRepo {
    val paymentExpirationTimeSpan: Duration
}