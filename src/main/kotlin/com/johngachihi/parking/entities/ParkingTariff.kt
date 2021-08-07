package com.johngachihi.parking.entities

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.Duration
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "parking_tariffs")
open class ParkingTariff {
    @Id
    @GeneratedValue
    open var id: Long? = null

    @Min(1)
    @Column(unique = true)
    open lateinit var upperLimit: Duration

    @Column(nullable = false)
    open var fee: Double = 0.0
}