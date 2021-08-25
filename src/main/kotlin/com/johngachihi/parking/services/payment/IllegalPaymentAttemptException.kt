package com.johngachihi.parking.services.payment

class IllegalPaymentAttemptException(override val message: String) : RuntimeException()