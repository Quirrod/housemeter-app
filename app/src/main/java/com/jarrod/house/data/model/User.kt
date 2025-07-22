package com.jarrod.house.data.model

data class User(
    val id: Int,
    val username: String,
    val role: String,
    val apartment_id: Int?,
    val house_id: Int?
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class ApiResponse(
    val message: String
)

data class FcmTokenRequest(
    val fcm_token: String
)

data class TestNotificationRequest(
    val title: String,
    val body: String
)

data class RegisterHouseAdminRequest(
    val username: String,
    val password: String,
    val house_name: String,
    val house_address: String,
    val house_description: String?
)

data class House(
    val id: Int,
    val name: String,
    val address: String,
    val description: String?
)

data class RegisterHouseAdminResponse(
    val message: String,
    val user: User,
    val house: House
)