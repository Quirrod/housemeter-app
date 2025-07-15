package com.jarrod.house.data.model

data class Floor(
    val id: Int,
    val floor_number: Int,
    val description: String?
)

data class Apartment(
    val id: Int,
    val floor_id: Int,
    val apartment_number: String,
    val meter_number: String,
    val floor_number: Int? = null
)