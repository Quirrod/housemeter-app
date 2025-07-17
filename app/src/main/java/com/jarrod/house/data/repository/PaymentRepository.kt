package com.jarrod.house.data.repository

import android.content.Context
import android.net.Uri
import com.jarrod.house.data.api.PaymentStatusRequest
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.model.Payment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class PaymentRepository(private val context: Context) {
    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getPayments(): Response<List<Payment>> {
        return apiService.getPayments()
    }

    suspend fun createPayment(
        debtId: Int,
        amount: Double,
        receiptUri: Uri?
    ): Response<Payment> {
        val debtIdBody = debtId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val amountBody = amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val notesBody = null // Optional notes field
        
        val receiptPart = receiptUri?.let { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "receipt_${System.currentTimeMillis()}")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("receipt", file.name, requestFile)
            } catch (e: Exception) {
                null
            }
        }
        
        return apiService.createPayment(debtIdBody, amountBody, notesBody, receiptPart)
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