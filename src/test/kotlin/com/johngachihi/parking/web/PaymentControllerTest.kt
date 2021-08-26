package com.johngachihi.parking.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.payment.Payment
import com.johngachihi.parking.minutesAgo
import com.johngachihi.parking.services.payment.PaymentService
import com.johngachihi.parking.services.payment.StartPaymentDto
import com.johngachihi.parking.services.payment.IllegalPaymentException
import com.johngachihi.parking.web.exceptionhandling.IllegalPaymentErrorResponse
import com.johngachihi.parking.web.exceptionhandling.InvalidTicketCodeErrorResponse
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

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
                makeStartPaymentRequest(StartPaymentDto(ticketCode = -1))
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
            private val startPaymentDto = StartPaymentDto(ticketCode = 1001)

            @Test
            fun `then uses PaymentService to start a payment session with the request input`() {
                makeStartPaymentRequest(startPaymentDto)

                verify(paymentService, times(1))
                    .startPayment(startPaymentDto)
            }

            @Test
            @DisplayName(
                "When attempt to start a payment session throws an IllegalPaymentException, " +
                        "then returns an illegal-payment error response"
            )
            fun testWhenIllegalPaymentExceptionThrown() {
                `when`(paymentService.startPayment(startPaymentDto))
                    .thenThrow(IllegalPaymentException("Exception message for IllegalPaymentException"))

                val expectedIllegalPaymentErrorResponse = IllegalPaymentErrorResponse(
                    "Exception message for IllegalPaymentException"
                )

                makeStartPaymentRequest(startPaymentDto)
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonEqualTo(expectedIllegalPaymentErrorResponse))
            }

            @Test
            @DisplayName(
                "When attempt to start a payment session throws an InvalidTicketCodeException, " +
                        "then returns an invalid-ticket-code error response"
            )
            fun testWhenInvalidTicketCodeExceptionThrown() {
                `when`(paymentService.startPayment(startPaymentDto))
                    .thenThrow(InvalidTicketCodeException("Exception message for InvalidTicketCodeException"))

                val expectedInvalidTicketCodeErrorResponse = InvalidTicketCodeErrorResponse(
                    "Exception message for InvalidTicketCodeException"
                )

                makeStartPaymentRequest(startPaymentDto)
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonEqualTo(expectedInvalidTicketCodeErrorResponse))
            }
        }

        @Test
        fun `When attempt to start a payment succeeds, then returns the new payment`() {
            val startPaymentDto = StartPaymentDto(ticketCode = 123)
            val expectedPayment = Payment().apply {
                status = Payment.Status.STARTED

                // FIXME: URGENT: This should be removed from here after
                //        dividing Payment entity into PendingPayment and CompletePayment.
                //        The Payment returned by this endpoint must be a PendingPayment
                //        and must therefore not have a finishedAt time.
                finishedAt = 10.minutesAgo
            }

            `when`(paymentService.startPayment(startPaymentDto))
                .thenReturn(expectedPayment)

            makeStartPaymentRequest(startPaymentDto)
                .andExpect(status().isOk)
                .andExpect(jsonEqualTo(expectedPayment))
        }


        private fun makeStartPaymentRequest(requestInput: StartPaymentDto): ResultActions {
            return mockMvc.perform(
                put("/payment/start-payment")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsBytes(requestInput))
            )
        }

        private fun jsonEqualTo(other: Any) = ResultMatcher {
            assertThat(it.response.contentType)
                .withFailMessage {
                    "Expected response Content-Type to be " +
                            "<application/json> but was: <${it.response.contentType}>"
                }
                .isEqualTo("application/json")

            assertThat(it.response.contentAsString)
                .withFailMessage(
                    "expected: <${objectMapper.writeValueAsString(other)}> " +
                            "but was: <${it.response.contentAsString}>"
                )
                .isEqualTo(objectMapper.writeValueAsString(other))
        }
    }
}