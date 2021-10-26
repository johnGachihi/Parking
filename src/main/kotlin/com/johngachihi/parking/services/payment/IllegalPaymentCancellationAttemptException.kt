package com.johngachihi.parking.services.payment

class IllegalPaymentCancellationAttemptException(override val message: String) : RuntimeException()
