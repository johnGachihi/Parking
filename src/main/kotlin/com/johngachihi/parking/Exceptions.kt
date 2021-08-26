package com.johngachihi.parking

class InvalidTicketCodeException(override val message: String) : RuntimeException()
class UnpaidFeeException(msg: String) : RuntimeException(msg)