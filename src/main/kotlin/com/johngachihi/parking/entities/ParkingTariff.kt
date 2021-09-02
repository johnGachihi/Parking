package com.johngachihi.parking.entities

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.validator.constraints.time.DurationMin
import java.time.Duration
import javax.persistence.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "parking_tariffs")
open class ParkingTariff {
    @Id
    @GeneratedValue
    open var id: Long? = null

    @DurationMin(minutes = 1, message = "The upper limit is required and must be equal to or more than a minute")
    @Column(unique = true)
    open var upperLimit: Duration = Duration.ofMinutes(0)

    @PositiveOrZero(message = "The fee is required and must be equal to or more than 0.0")
    @Column(nullable = false)
    open var fee: Double = -1.0
}