package com.johngachihi.parking.web

import org.hamcrest.CoreMatchers
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


// TODO: Improve error message for when field or validation keys are not among
//       json response
fun hasViolation(field: String, errorMsg: String): ResultMatcher =
    MockMvcResultMatchers.jsonPath("$.violations.$field", CoreMatchers.hasItem(errorMsg))