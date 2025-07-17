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
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when {
                    mimeType.contains("png") -> ".png"
                    mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                    mimeType.contains("pdf") -> ".pdf"
                    else -> ".jpg"
                }
                val file = File(context.cacheDir, "receipt_${System.currentTimeMillis()}$extension")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Log file details for debugging
                println("PaymentRepository: Preparing file upload")
                println("  URI: $uri")
                println("  MIME type: $mimeType")
                println("  File name: ${file.name}")
                println("  File size: ${file.length()} bytes")
                println("  File exists: ${file.exists()}")
                
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("receipt", file.name, requestFile)
            } catch (e: Exception) {
                println("PaymentRepository: Error preparing file upload: ${e.message}")
                e.printStackTrace()
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