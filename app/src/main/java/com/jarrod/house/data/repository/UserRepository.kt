package com.jarrod.house.data.repository

import android.content.Context
import com.jarrod.house.data.api.CreateUserRequest
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.api.UpdateUserRequest
import com.jarrod.house.data.model.User
import retrofit2.Response

class UserRepository(private val context: Context) {
    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getUsers(): Response<List<User>> {
        return apiService.getUsers()
    }

    suspend fun createUser(
        username: String,
        password: String,
        role: String,
        apartmentId: Int?
    ): Response<User> {
        val request = CreateUserRequest(username, password, role, apartmentId)
        return apiService.createUser(request)
    }

    suspend fun updateUser(
        id: Int,
        username: String,
        password: String?,
        role: String,
        apartmentId: Int?
    ): Response<com.jarrod.house.data.api.ApiResponse> {
        val request = UpdateUserRequest(username, password, role, apartmentId)
        return apiService.updateUser(id, request)
    }

    suspend fun deleteUser(id: Int): Response<com.jarrod.house.data.api.ApiResponse> {
        return apiService.deleteUser(id)
    }
}