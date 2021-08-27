package com.johngachihi.parking.entities.payment

import com.johngachihi.parking.entities.visit.Visit
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

// TODO: For all entities', synchronize nullability status in DB

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