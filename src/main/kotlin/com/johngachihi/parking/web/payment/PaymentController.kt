package com.johngachihi.parking.web.payment

import com.johngachihi.parking.services.payment.CompletePaymentDto
import com.johngachihi.parking.services.payment.PaymentService
import com.johngachihi.parking.services.payment.StartPaymentDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@CrossOrigin
@RequestMapping("/payment")
class PaymentController(
    @Autowired
    private val paymentService: PaymentService,
    @Autowired
    private val startPaymentOutputDtoAssembler: StartPaymentOutputDtoAssembler
) {
    @PutMapping("/start-payment")
    fun startPayment(@Valid @RequestBody input: StartPaymentDto) =
        startPaymentOutputDtoAssembler.assemble(
            paymentService.startPayment(input)
        )

    @PutMapping("/complete-payment")
    fun completePayment(@Valid @RequestBody input: CompletePaymentDto) {
        paymentService.completePayment(input)
    }
}



