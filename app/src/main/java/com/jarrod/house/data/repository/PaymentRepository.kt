package com.jarrod.house.data.repository

import android.content.Context
import com.jarrod.house.data.api.PaymentStatusRequest
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.model.Payment
import retrofit2.Response

class PaymentRepository(private val context: Context) {
    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getPayments(): Response<List<Payment>> {
        return apiService.getPayments()
    }

    suspend fun updatePaymentStatus(
        id: Int,
        status: String,
        notes: String?
    ): Response<com.jarrod.house.data.api.ApiResponse> {
        val request = PaymentStatusRequest(status, notes)
        return apiService.updatePaymentStatus(id, request)
    }

    suspend fun deletePayment(id: Int): Response<com.jarrod.house.data.api.ApiResponse> {
        return apiService.deletePayment(id)
    }
}