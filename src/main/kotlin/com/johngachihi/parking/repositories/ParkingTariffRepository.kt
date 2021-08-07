package com.johngachihi.parking.repositories

import com.johngachihi.parking.entities.ParkingTariff
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ParkingTariffRepository : JpaRepository<ParkingTariff, Long> {
    @Query("select p from ParkingTariff p order by p.upperLimit asc")
    fun getAllInAscOrderOfUpperLimit(): List<ParkingTariff>
}