package com.johngachihi.parking.services.payment

import javax.validation.constraints.Positive

data class StartPaymentDto(
    @field:Positive(message = "A ticket code is required and must be greater than zero")
    val ticketCode: Long,

//    @field:[NotNull Min(2)]
//    val vendorId: Long? = null,
)