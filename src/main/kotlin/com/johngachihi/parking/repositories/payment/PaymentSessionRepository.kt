package com.johngachihi.parking.repositories.payment

import com.johngachihi.parking.entities.payment.PaymentSession
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentSessionRepository : JpaRepository<PaymentSession, Long>