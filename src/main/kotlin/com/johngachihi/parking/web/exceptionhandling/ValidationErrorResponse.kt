package com.johngachihi.parking.web.exceptionhandling

import org.springframework.http.HttpStatus

data class ValidationErrorResponse(
    val violations: Map<String, List<String?>>,
    override val detail: String = "The inputs for the following fields are invalid: ${violations.keys}",
) : ErrorResponse() {
    override val type: String = "validation-error"
    override val status: HttpStatus = HttpStatus.BAD_REQUEST
    override val title: String = "Validation error"
}