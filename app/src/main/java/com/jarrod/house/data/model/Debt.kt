package com.jarrod.house.data.model

data class Debt(
    val id: Int,
    val apartment_id: Int,
    val amount: Double,
    val description: String?,
    val due_date: String?,
    val status: String,
    val created_at: String,
    val apartment_number: String? = null,
    val meter_number: String? = null,
    val floor_number: Int? = null
)