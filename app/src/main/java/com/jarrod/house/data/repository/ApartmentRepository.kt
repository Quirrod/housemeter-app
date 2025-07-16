package com.jarrod.house.data.repository

import android.content.Context
import com.jarrod.house.data.api.CreateApartmentRequest
import com.jarrod.house.data.api.CreateFloorRequest
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.api.UpdateApartmentRequest
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.data.model.Floor
import retrofit2.Response

class ApartmentRepository(private val context: Context) {
    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getApartments(): Response<List<Apartment>> {
        return apiService.getApartments()
    }

    suspend fun createApartment(
        floorId: Int,
        apartmentNumber: String,
        meterNumber: String
    ): Response<Apartment> {
        val request = CreateApartmentRequest(floorId, apartmentNumber, meterNumber)
        return apiService.createApartment(request)
    }

    suspend fun updateApartment(
        id: Int,
        floorId: Int,
        apartmentNumber: String,
        meterNumber: String
    ): Response<com.jarrod.house.data.api.ApiResponse> {
        val request = UpdateApartmentRequest(floorId, apartmentNumber, meterNumber)
        return apiService.updateApartment(id, request)
    }

    suspend fun deleteApartment(id: Int): Response<com.jarrod.house.data.api.ApiResponse> {
        return apiService.deleteApartment(id)
    }

    suspend fun getFloors(): Response<List<Floor>> {
        return apiService.getFloors()
    }

    suspend fun createFloor(
        floorNumber: Int,
        description: String?
    ): Response<Floor> {
        val request = CreateFloorRequest(floorNumber, description)
        return apiService.createFloor(request)
    }

    suspend fun updateFloor(
        id: Int,
        floorNumber: Int,
        description: String?
    ): Response<com.jarrod.house.data.api.ApiResponse> {
        val request = com.jarrod.house.data.api.UpdateFloorRequest(floorNumber, description)
        return apiService.updateFloor(id, request)
    }

    suspend fun deleteFloor(id: Int): Response<com.jarrod.house.data.api.ApiResponse> {
        return apiService.deleteFloor(id)
    }
}