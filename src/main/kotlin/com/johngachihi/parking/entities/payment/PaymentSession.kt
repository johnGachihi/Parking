package com.johngachihi.parking.entities.payment

import com.johngachihi.parking.entities.visit.Visit
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "payment_sessions")
open class PaymentSession {
    @Id
    @GeneratedValue
    open var id: Long? = null

    @NotNull
    @CreationTimestamp
    @Column(name = "started_at", nullable = false)
    open var startedAt: Instant = Instant.now()

    @NotNull
    @Column(nullable = false)
    open var amount: Double? = null

    @NotNull
    @ManyToOne(cascade = [CascadeType.ALL], optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    open lateinit var visit: Visit

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open lateinit var status: Status

    @Column(name = "finished_at")
    open var finishedAt: Instant? = null


    enum class Status {
        PENDING,
        COMPLETED,
        CANCELLED,
    }
}