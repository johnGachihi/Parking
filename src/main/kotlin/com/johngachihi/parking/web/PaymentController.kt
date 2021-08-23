package com.johngachihi.parking.web

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.johngachihi.parking.services.payment.StartPaymentDto
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/payment")
class PaymentController {
    @JsonAnyGetter
    @PutMapping("/start-payment")
    fun startPayment(@Valid @RequestBody input: StartPaymentDto) {
    }
}



