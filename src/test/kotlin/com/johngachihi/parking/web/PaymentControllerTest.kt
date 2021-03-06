package com.johngachihi.parking.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.johngachihi.parking.InvalidTicketCodeException
import com.johngachihi.parking.entities.payment.PaymentSession
import com.johngachihi.parking.entities.visit.OngoingVisit
import com.johngachihi.parking.minutes
import com.johngachihi.parking.services.payment.CompletePaymentDto
import com.johngachihi.parking.services.payment.IllegalPaymentAttemptException
import com.johngachihi.parking.services.payment.IllegalPaymentCancellationAttemptException
import com.johngachihi.parking.services.payment.PaymentService
import com.johngachihi.parking.services.payment.StartPaymentDto
import com.johngachihi.parking.web.exceptionhandling.IllegalPaymentAttemptErrorResponse
import com.johngachihi.parking.web.exceptionhandling.IllegalPaymentCancellationAttemptErrorResponse
import com.johngachihi.parking.web.exceptionhandling.InvalidTicketCodeErrorResponse
import com.johngachihi.parking.web.payment.CancelPaymentDto
import com.johngachihi.parking.web.payment.PaymentController
import com.johngachihi.parking.web.payment.StartPaymentOutputDto
import com.johngachihi.parking.web.payment.StartPaymentOutputDtoAssembler
import org.assertj.core.api.Assertions.assertThat
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(controllers = [PaymentController::class])
internal class PaymentControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var paymentService: PaymentService

    @MockBean
    private lateinit var startPaymentOutputDtoAssembler: StartPaymentOutputDtoAssembler

    @Nested
    @DisplayName("Test /payment/start-payment")
    inner class TestStartPaymentEndpoint {

        @Nested
        @DisplayName("Test input validation")
        inner class TestInputValidation {
            @Test
            fun `When request lacks a ticketCode, then returns a 400 response with validation error`() {
                makeRequest("/payment/start-payment", "{}")
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            field = "ticketCode",
                            errorMsg = "A ticket code is required and must be greater than zero"
                        )
                    )
            }

            @Test
            @DisplayName(
                "When request has a ticketCode that is less than 0," +
                        "then returns a 400 response with validation error"
            )
            fun whenRequestHasTicketCodeIsLessThanZero() {
                makeStartPaymentRequest(StartPaymentDto(ticketCode = -1))
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            field = "ticketCode",
                            errorMsg = "A ticket code is required and must be greater than zero"
                        )
                    )
            }
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
                "When attempt to start a payment session throws an IllegalPaymentAttemptException, " +
                        "then returns an illegal-payment error response"
            )
            fun testWhenIllegalPaymentExceptionThrown() {
                `when`(paymentService.startPayment(startPaymentDto))
                    .thenThrow(IllegalPaymentAttemptException("Exception message for IllegalPaymentAttemptException"))

                val expectedIllegalPaymentAttemptErrorResponse = IllegalPaymentAttemptErrorResponse(
                    "Exception message for IllegalPaymentAttemptException"
                )

                makeStartPaymentRequest(startPaymentDto)
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonEqualTo(expectedIllegalPaymentAttemptErrorResponse))
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

            @Test
            @DisplayName(
                "When attempt to start a payment session succeeds," +
                        "then returns appropriate StartPaymentOutputDto"
            )
            fun testWhenStartingPaymentSessionSucceeds() {
                val startPaymentDto = StartPaymentDto(ticketCode = 123)

                val paymentSession = makePaymentSession()
                val expectedStartPaymentOutputDto = StartPaymentOutputDto(
                    paymentSessionId = 1,
                    paymentSessionExpiryTime = Instant.now().plus(10.minutes),
                    paymentAmount = 100.0,
                    ongoingVisitTimeOfStay = 20.minutes,
                    ongoingVisitEntryTime = Instant.now().minus(20.minutes),
                    ongoingVisitLatestPaymentTime = null
                )

                `when`(paymentService.startPayment(startPaymentDto))
                    .thenReturn(paymentSession)

                `when`(startPaymentOutputDtoAssembler.assemble(paymentSession))
                    .thenReturn(expectedStartPaymentOutputDto)

                makeStartPaymentRequest(startPaymentDto)
                    .andExpect(status().isOk)
                    .andExpect(jsonEqualTo(expectedStartPaymentOutputDto))
            }
        }


        private fun makeStartPaymentRequest(requestInput: StartPaymentDto): ResultActions {
            return makeRequest("/payment/start-payment", requestInput)
        }

        private fun makePaymentSession() = PaymentSession().apply {
            visit = OngoingVisit()
            status = PaymentSession.Status.PENDING
        }
    }

    @Nested
    @DisplayName("Test /payment/complete-payment")
    inner class TestCompletePaymentEndpoint {
        private val completePaymentDto = CompletePaymentDto(paymentSessionId = 123)

        @Nested
        @DisplayName("Test input validation")
        inner class TestInputValidation {
            @Test
            fun `When request lacks paymentSessionId field, then returns a 400 and validation error response`() {
                makeRequest("/payment/complete-payment", "{\"a\": 1}")
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "paymentSessionId",
                            "A payment-session ID is required and must be greater than zero"
                        )
                    )
            }

            @Test
            @DisplayName(
                "When request has paymentSessionId input that is less than 1, " +
                        "then return a 400 and appropriate validation error message"
            )
            fun testWhenPaymentSessionIdInputLessThan1() {
                makeCompletePaymentRequest(CompletePaymentDto(paymentSessionId = -1))
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "paymentSessionId",
                            "A payment-session ID is required and must be greater than zero"
                        )
                    )
            }
        }

        @Test
        fun `Calls PaymentService#completeService`() {
            makeCompletePaymentRequest(completePaymentDto)

            verify(paymentService, times(1))
                .completePayment(completePaymentDto)
        }

        @Test
        @DisplayName(
            "When attempt to complete payment throws IllegalPaymentAttemptException, " +
                    "then returns an illegal-payment-attempt response " +
                    "with detail as the exception's message"
        )
        fun testWhenIllegalPaymentAttemptExceptionThrown() {
            val exceptionMessage = "Illegal payment attempt exception message"

            `when`(paymentService.completePayment(completePaymentDto))
                .thenThrow(IllegalPaymentAttemptException(exceptionMessage))

            val expectedErrorResponse = IllegalPaymentAttemptErrorResponse(
                detail = exceptionMessage
            )

            makeCompletePaymentRequest(completePaymentDto)
                .andExpect(status().isBadRequest)
                .andExpect(jsonEqualTo(expectedErrorResponse))
        }


        private fun makeCompletePaymentRequest(requestInput: CompletePaymentDto): ResultActions {
            return makeRequest("/payment/complete-payment", requestInput)
        }
    }

    @Nested
    @DisplayName("Test /payment/cancel-payment")
    inner class TestCancelPaymentEndpoint {
        @Nested
        @DisplayName("Test input validation")
        inner class TestInputValidation {
            @Test
            @DisplayName(
                "When paymentSessionId is not Provided, then returns a 400" +
                        "response and appropriate validation error message"
            )
            fun testWhenPaymentSessionIdNotProvided() {
                makeRequest("/payment/cancel-payment", "{}")
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "paymentSessionId",
                            "A payment-session ID is required and must be greater than zero"
                        )
                    )
            }

            @Test
            @DisplayName(
                "When paymentSessionId is below 1, then returns a 400 " +
                        "response with appropriate validation error message"
            )
            fun testWhenPaymentSessionIdProvidedBelowOne() {
                makeCancelPaymentRequest(CancelPaymentDto(paymentSessionId = -1))
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "paymentSessionId",
                            "A payment-session ID is required and must be greater than zero"
                        )
                    )
            }
        }

        @Test
        fun `When request data is valid, then uses PaymentService#cancelPayment to attempt cancellation`() {
            val paymentSessionId = 1L

            makeCancelPaymentRequest(CancelPaymentDto(paymentSessionId))
                .andExpect(status().isOk)

            verify(paymentService, times(1))
                .cancelPayment(paymentSessionId)
        }

        @Test
        @DisplayName(
            "When attempt to cancel payment throws IllegalPaymentCancellationAttemptException, " +
                    "then returns 400 response with appropriate error message"
        )
        fun testWhenCancellationAttemptThrowsIllegalPaymentCancellationAttemptException() {
            val paymentSessionId = 1L

            val errorResponse = IllegalPaymentCancellationAttemptErrorResponse(
                "Illegal payment cancellation"
            )

            `when`(paymentService.cancelPayment(paymentSessionId))
                .thenThrow(IllegalPaymentCancellationAttemptException(errorResponse.detail))

            makeCancelPaymentRequest(CancelPaymentDto(paymentSessionId))
                .andExpect(status().isBadRequest)
                .andExpect(jsonEqualTo(errorResponse))
        }

        private fun makeCancelPaymentRequest(requestInput: CancelPaymentDto) =
            makeRequest("/payment/cancel-payment", requestInput)
    }


    private fun makeRequest(url: String, requestInput: Any): ResultActions {
        return mockMvc.perform(
            put(url)
                .contentType("application/json")
                .content(
                    if (requestInput is String) requestInput
                    else objectMapper.writeValueAsString(requestInput)
                )
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
