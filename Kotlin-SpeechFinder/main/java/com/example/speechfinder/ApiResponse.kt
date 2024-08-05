package com.example.speechfinder


data class ApiResponse(
    val ok: Boolean,
    val message: String,
    val data: Data?,
    val errors: Any?
)

data class Data(
    val numbers: List<Int>
)