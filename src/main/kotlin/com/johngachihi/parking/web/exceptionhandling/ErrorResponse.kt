package com.johngachihi.parking.web.exceptionhandling

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.http.HttpStatus

@JsonPropertyOrder(value = ["type", "title", "status", "detail"])
abstract class ErrorResponse {
    abstract val type: String
    abstract val title: String

    @get:JsonIgnore
    abstract val status: HttpStatus

    @get:JsonProperty("status")
    val statusCode: Int
        get() = status.value()

    abstract val detail: String
}