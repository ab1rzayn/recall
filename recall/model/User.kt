package com.example.recall.model

data class User(
    val id:String ="",
    val name: String,
    val email: String? ="",
    val password: String? ="",
    val phone: String?="",
    val CreatedAt: Long = System.currentTimeMillis()
)
