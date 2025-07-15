package com.jarrod.house.data.api

import com.jarrod.house.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("apartments")
    suspend fun getApartments(): Response<List<Apartment>>

    @GET("debts")
    suspend fun getDebts(): Response<List<Debt>>

    @POST("debts")
    suspend fun createDebt(@Body debt: DebtRequest): Response<Debt>

    @PUT("debts/{id}")
    suspend fun updateDebt(@Path("id") id: Int, @Body debt: DebtRequest): Response<ApiResponse>

    @DELETE("debts/{id}")
    suspend fun deleteDebt(@Path("id") id: Int): Response<ApiResponse>

    @GET("payments")
    suspend fun getPayments(): Response<List<Payment>>

    @Multipart
    @POST("payments")
    suspend fun createPayment(
        @Part("debt_id") debtId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("notes") notes: RequestBody?,
        @Part receipt: MultipartBody.Part?
    ): Response<Payment>

    @PUT("payments/{id}/status")
    suspend fun updatePaymentStatus(
        @Path("id") id: Int,
        @Body request: PaymentStatusRequest
    ): Response<ApiResponse>

    @GET("metrics/payments")
    suspend fun getPaymentMetrics(
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): Response<PaymentMetrics>

    @GET("metrics/history")
    suspend fun getPaymentHistory(
        @Query("apartment_id") apartmentId: Int?,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<Payment>>

    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfile>
}

data class DebtRequest(
    val apartment_id: Int,
    val amount: Double,
    val description: String?,
    val due_date: String?
)

data class PaymentStatusRequest(
    val status: String,
    val notes: String?
)

data class ApiResponse(
    val message: String
)

data class PaymentMetrics(
    val totalRevenue: Double,
    val paymentsByStatus: List<StatusCount>,
    val debtsByStatus: List<StatusCount>,
    val paymentsByApartment: List<ApartmentPayments>
)

data class StatusCount(
    val status: String,
    val count: Int,
    val total: Double
)

data class ApartmentPayments(
    val apartment_number: String,
    val floor_number: Int,
    val payment_count: Int,
    val total_paid: Double
)

data class UserProfile(
    val id: Int,
    val username: String,
    val role: String,
    val apartment_id: Int?,
    val created_at: String,
    val apartment_number: String?,
    val meter_number: String?,
    val floor_number: Int?
)