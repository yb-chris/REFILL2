package com.example.refill

data class Order(
    val id: String = "",
    val userId: String = "",
    val bottleSize: String = "",
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val status: String = ""
)