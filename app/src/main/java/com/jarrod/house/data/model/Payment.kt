package com.jarrod.house.data.model

data class Payment(
    val id: Int,
    val debt_id: Int,
    val amount: Double,
    val payment_date: String,
    val receipt_path: String?,
    val status: String,
    val approved_by: Int?,
    val approved_at: String?,
    val notes: String?,
    val debt_amount: Double? = null,
    val debt_description: String? = null,
    val apartment_number: String? = null,
    val meter_number: String? = null,
    val floor_number: Int? = null,
    val approved_by_username: String? = null
)