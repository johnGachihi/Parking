package com.johngachihi.parking.entities

import com.johngachihi.parking.entities.visit.Visit
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity(name = "payments")
open class Payment {
    @Id
    @GeneratedValue
    open val id: Long? = null

    @NotNull
    @ManyToOne
    open var visit: Visit? = null

    @NotNull
    @CreationTimestamp
    open var madeAt: Instant = Instant.now()

    @NotNull
    open var amount: Double? = null
}