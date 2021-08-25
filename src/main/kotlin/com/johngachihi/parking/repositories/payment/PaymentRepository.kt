package com.johngachihi.parking.repositories.payment

import com.johngachihi.parking.entities.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long>