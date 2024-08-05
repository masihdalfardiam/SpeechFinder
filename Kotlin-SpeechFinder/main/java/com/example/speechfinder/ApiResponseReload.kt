package com.example.speechfinder

data class ApiResponseReload(
    val ok: Boolean,
    val message: String,
    val data: DataReload,
    val errors: Any?
)