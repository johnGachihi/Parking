package com.johngachihi.parking.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.minutes
import com.johngachihi.parking.services.parkingTariffSettings.ParkingTariffSettingsService
import com.johngachihi.parking.web.exceptionhandling.InvalidInputDataFormatErrorResponse
import com.johngachihi.parking.web.parkingTariffSettings.OverwriteParkingTariffSettingsDto
import com.johngachihi.parking.web.parkingTariffSettings.ParkingTariffSettingsController
import org.assertj.core.api.Assertions
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

@DisplayName("Test /settings/parking-tariff endpoint")
@WebMvcTest(controllers = [ParkingTariffSettingsController::class])
internal class ParkingTariffSettingsControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var parkingTariffSettingsService: ParkingTariffSettingsService

    @Test
    fun `GET returns response with parking tariff data`() {
        val parkingTariffData = listOf(
            ParkingTariff().apply { upperLimit = 10.minutes },
            ParkingTariff().apply { upperLimit = 20.minutes }
        )
        `when`(parkingTariffSettingsService.getParkingTariffSettings())
            .thenReturn(parkingTariffData)

        mockMvc.perform(get("/settings/parking-tariff"))
            .andExpect(jsonEqualTo(parkingTariffData))
    }

    @Nested
    @DisplayName("Test PUT")
    inner class TestPut {
        @Nested
        @DisplayName("Test input validation")
        inner class TestInputValidation {
            @Test
            @DisplayName(
                "When input lacks parking tariff data, then returns " +
                        "400 response with appropriate validation error messages"
            )
            fun testWhenInputLacksParkingTariffData() {
                makeRequest("{}")
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "newParkingTariffSettings",
                            "Parking tariff settings is required"
                        )
                    )
            }

            @Test
            @DisplayName(
                "When newParkingTariffSettings param is not an array, " +
                        "then return 400 response with appropriate error message"
            )
            fun testWhenNewParkingTariffSettingsParamIsNotArray() {
                makeRequest("{\"newParkingTariffSettings\": \"a\"}")
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonEqualTo(InvalidInputDataFormatErrorResponse()))
            }

            @Test
            @DisplayName(
                "When newParkingTariffSettings param contains a ParkingTariff with invalid upperLimit, " +
                        "then returns 400 response with appropriate error message"
            )
            fun testWhenNewParkingTariffSettingsContainsInvalidParkingTariffs() {
                val requestInput = OverwriteParkingTariffSettingsDto(
                    newParkingTariffSettings = listOf(
                        ParkingTariff().apply { upperLimit = Duration.ofMinutes(0) },
                        ParkingTariff().apply { upperLimit = Duration.ofMinutes(1); fee = 1.0 }
                    )
                )

                makeRequest(requestInput)
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "[\"newParkingTariffSettings[0].upperLimit\"]",
                            "The upper limit is required and must be equal to or more than a minute"
                        )
                    )
            }

            @Test
            @DisplayName(
                "When newParkingTariffSettings contains a ParkingTariff with an invalid fee property, " +
                        "then returns a 400 response with appropriate error message"
            )
            fun testWhenNewParkingTariffSettingsContainsParkingTariffWithInvalidFee() {
                val requestInput = OverwriteParkingTariffSettingsDto(
                    newParkingTariffSettings = listOf(
                        ParkingTariff().apply { },
                        ParkingTariff().apply { upperLimit = Duration.ofMinutes(1); fee = 1.0 }
                    )
                )

                makeRequest(requestInput)
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "[\"newParkingTariffSettings[0].fee\"]",
                            "The fee is required and must be equal to or more than 0.0"
                        )
                    )
            }

            @Test
            @DisplayName(
                "When newParkingTariffSettings has ParkingTariff entities with " +
                        "similar (not unique) upperLimits, then returns a 400 " +
                        "response with appropriate error message"
            )
            fun testWhenNewParkingTariffSettingsHasParkingTariffsWithSimilarUpperLimits() {
                val requestInput = OverwriteParkingTariffSettingsDto(
                    newParkingTariffSettings = listOf(
                        ParkingTariff().apply { upperLimit = 10.minutes },
                        ParkingTariff().apply { upperLimit = 10.minutes },
                    )
                )

                makeRequest(requestInput)
                    .andExpect(status().isBadRequest)
                    .andExpect(
                        hasViolation(
                            "newParkingTariffSettings",
                            "Upper limits must be unique"
                        )
                    )
            }
        }

        @Nested
        @DisplayName("When input is valid")
        inner class TestWhenInputValid {
            private val requestInput = OverwriteParkingTariffSettingsDto(
                newParkingTariffSettings = listOf(
                    ParkingTariff().apply { upperLimit = 10.minutes; fee = 10.0 },
                    ParkingTariff().apply { upperLimit = 20.minutes; fee = 20.0 }
                )
            )

            @Test
            fun `Then attempts to overwrite parking-tariff settings`() {
                makeRequest(requestInput)

                verify(parkingTariffSettingsService, times(1))
                    .overwriteParkingTariffSettings(anyList())
            }
        }

        private fun makeRequest(requestInput: Any): ResultActions {
            return mockMvc.perform(
                put("/settings/parking-tariff")
                    .contentType("application/json")
                    .content(
                        if (requestInput is String) requestInput
                        else objectMapper.writeValueAsString(requestInput)
                    )
            )
        }
    }


    // TODO: DRY
    private fun jsonEqualTo(other: Any) = ResultMatcher {
        Assertions.assertThat(it.response.contentType)
            .withFailMessage {
                "Expected response Content-Type to be " +
                        "<application/json> but was: <${it.response.contentType}>"
            }
            .isEqualTo("application/json")

        Assertions.assertThat(it.response.contentAsString)
            .withFailMessage(
                "expected: <${objectMapper.writeValueAsString(other)}> " +
                        "but was: <${it.response.contentAsString}>"
            )
            .isEqualTo(objectMapper.writeValueAsString(other))
    }
}