package com.johngachihi.parking.entities.visit

import com.johngachihi.parking.entities.Payment
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity(name = "visits")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
open class Visit {
    @Id
    @GeneratedValue
    open var id: Long? = null

    @NotNull
    open var entryTime: Instant = Instant.now()

    @NotNull
    open var ticketCode: Long = 0L

    @OneToMany(
        mappedBy = "visit",
        targetEntity = Payment::class,
        cascade = [CascadeType.ALL]
    )
    open var payments: List<Payment> = emptyList()
}

@Entity
open class OngoingVisit : Visit()

@Entity
open class FinishedVisit : Visit() {
    @CreationTimestamp
    open var exitTime: Instant = Instant.now()
}
