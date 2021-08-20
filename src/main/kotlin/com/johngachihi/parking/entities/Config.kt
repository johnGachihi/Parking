package com.johngachihi.parking.entities

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "configuration")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
open class Config {
    @Id
    @GeneratedValue
    open var id: Long? = null

    @NotNull
    @Column(name = "`key`", unique = true)
    open lateinit var key: String

    @NotNull
    @Column(name = "`value`")
    open lateinit var value: String
}

// TODO: See why the @Table annotation causes warning when bootstrapping Hibernate
@Entity
//@Table(name = "parking_fee_configuration")
open class ParkingFeeConfig : Config()