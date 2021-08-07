package com.johngachihi.parking

class InvalidTicketCodeException(msg: String) : Exception(msg)
class UnpaidFeeException(msg: String) : Exception(msg)