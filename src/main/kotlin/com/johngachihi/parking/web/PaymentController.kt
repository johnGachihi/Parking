package com.johngachihi.parking.web

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.johngachihi.parking.services.payment.PaymentService
import com.johngachihi.parking.services.payment.StartPaymentDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/payment")
class PaymentController(
    @Autowired
    private val paymentService: PaymentService
) {
    @JsonAnyGetter
    @PutMapping("/start-payment")
    fun startPayment(@Valid @RequestBody input: StartPaymentDto) =
        paymentService.startPayment(input)
}



