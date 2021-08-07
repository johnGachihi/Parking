package com.johngachihi.parking.services

import com.johngachihi.parking.entities.OngoingVisit
import com.johngachihi.parking.entities.visit.timeOfStay
import com.johngachihi.parking.repositories.ParkingFeeConfigRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface PaymentService {
    fun calculateParkingFee(ongoingVisit: OngoingVisit): Double
}

@Service
class DefaultPaymentService(
    @Autowired
    private val parkingTariffService: ParkingTariffService,
    @Autowired
    private val parkingFeeConfigRepo: ParkingFeeConfigRepo
) : PaymentService {
    override fun calculateParkingFee(ongoingVisit: OngoingVisit): Double {
        if (ongoingVisit.payments.isEmpty())
            return parkingTariffService.getFee(ongoingVisit.timeOfStay)

        return 0.1
    }
}