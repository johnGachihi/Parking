package com.johngachihi.parking.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.johngachihi.parking.services.payment.PaymentService
import com.johngachihi.parking.services.payment.StartPaymentDto
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [PaymentController::class])
internal class PaymentControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var paymentService: PaymentService

    @Nested
    @DisplayName("Test /payment/start-payment")
    inner class TestStartPaymentEndpoint {

        @Nested
        @DisplayName("Test input validation")
        inner class TestInputValidation {
            @Test
            fun `When request lacks a ticketCode, then returns a 400 response with validation error`() {
                mockMvc.perform(
                    put("/payment/start-payment")
                        .contentType("application/json")
                        .content("{}")
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            field = "ticketCode",
                            errorMsg = "A ticket code is required and must be greater than zero"
                        )
                    )
            }

            @Test
            fun `When request has a ticketCode that is less than 0, then returns a 400 response with validation error`() {
                mockMvc.perform(
                    put("/payment/start-payment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(StartPaymentDto(ticketCode = -1)))
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            field = "ticketCode",
                            errorMsg = "A ticket code is required and must be greater than zero"
                        )
                    )
            }

            private fun hasViolation(field: String, errorMsg: String): ResultMatcher = jsonPath(
                "$.violations.$field",
                hasItem(errorMsg)
            )
        }

        @Nested
        @DisplayName("When request is valid")
        inner class TestWhenRequestIsValid {
            @Test
            fun `then uses PaymentService to start a payment session with the request input`() {
                val startPaymentDto = StartPaymentDto(ticketCode = 1001)
                mockMvc.perform(
                    put("/payment/start-payment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(startPaymentDto))
                )

                verify(paymentService, times(1))
                    .startPayment(startPaymentDto)
            }

            @Test
            fun `And attempt to start a payment session throws a VisitInExitAllowancePeriodException, then returns a `() {
            }
        }
    }
}