package com.jarrod.house.data.repository

import android.content.Context
import com.jarrod.house.data.api.DebtRequest
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.model.Debt
import retrofit2.Response

class DebtRepository(private val context: Context) {
    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getDebts(): Response<List<Debt>> {
        return apiService.getDebts()
    }

    suspend fun createDebt(
        apartmentId: Int,
        amount: Double,
        description: String?,
        dueDate: String?
    ): Response<Debt> {
        val request = DebtRequest(apartmentId, amount, description, dueDate)
        return apiService.createDebt(request)
    }

    suspend fun updateDebt(
        id: Int,
        apartmentId: Int,
        amount: Double,
        description: String?,
        dueDate: String?
    ): Response<com.jarrod.house.data.api.ApiResponse> {
        val request = DebtRequest(apartmentId, amount, description, dueDate)
        return apiService.updateDebt(id, request)
    }

    suspend fun deleteDebt(id: Int): Response<com.jarrod.house.data.api.ApiResponse> {
        return apiService.deleteDebt(id)
    }
}