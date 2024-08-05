package com.example.speechfinder.data

data class LoginResponse(
    val ok: Boolean,
    val message: String,
    val data: Data?,
    val errors: Any?
)

data class Data(
    val token: String,
    val status: Int
)