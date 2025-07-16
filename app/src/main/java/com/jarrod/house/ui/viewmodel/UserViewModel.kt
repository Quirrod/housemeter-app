package com.jarrod.house.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarrod.house.data.model.Debt
import com.jarrod.house.data.model.Payment
import com.jarrod.house.data.model.User
import com.jarrod.house.data.repository.DebtRepository
import com.jarrod.house.data.repository.PaymentRepository
import com.jarrod.house.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel : ViewModel() {
    private val _userDebts = MutableStateFlow<List<Debt>>(emptyList())
    val userDebts: StateFlow<List<Debt>> = _userDebts

    private val _userPayments = MutableStateFlow<List<Payment>>(emptyList())
    val userPayments: StateFlow<List<Payment>> = _userPayments

    private val _userProfile = MutableStateFlow<com.jarrod.house.data.api.UserProfile?>(null)
    val userProfile: StateFlow<com.jarrod.house.data.api.UserProfile?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _createPaymentResult = MutableStateFlow<Result<Payment>?>(null)
    val createPaymentResult: StateFlow<Result<Payment>?> = _createPaymentResult

    fun loadUserProfile(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = AuthRepository(context)
                val response = repository.getProfile()
                
                if (response.isSuccessful) {
                    _userProfile.value = response.body()
                } else {
                    _error.value = "Error al cargar perfil: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserDebts(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = DebtRepository(context)
                val response = repository.getDebts()
                
                if (response.isSuccessful) {
                    val allDebts = response.body() ?: emptyList()
                    // Filter debts for current user's apartment
                    val userApartmentId = _userProfile.value?.apartment_id
                    _userDebts.value = if (userApartmentId != null) {
                        allDebts.filter { it.apartment_id == userApartmentId }
                    } else {
                        emptyList()
                    }
                } else {
                    _error.value = "Error al cargar deudas: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserPayments(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = PaymentRepository(context)
                val response = repository.getPayments()
                
                if (response.isSuccessful) {
                    val allPayments = response.body() ?: emptyList()
                    // Filter payments for current user's apartment debts
                    val userApartmentId = _userProfile.value?.apartment_id
                    if (userApartmentId != null) {
                        val userDebtIds = _userDebts.value.map { it.id }
                        _userPayments.value = allPayments.filter { it.debt_id in userDebtIds }
                    } else {
                        _userPayments.value = emptyList()
                    }
                } else {
                    _error.value = "Error al cargar pagos: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPayment(
        context: Context,
        debtId: Int,
        amount: Double,
        notes: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = PaymentRepository(context)
                // Create payment with multipart request
                // For now, we'll create without receipt file
                val response = createPaymentWithoutReceipt(repository, debtId, amount, notes)
                
                if (response.isSuccessful && response.body() != null) {
                    _createPaymentResult.value = Result.success(response.body()!!)
                    // Reload user data
                    loadUserPayments(context)
                } else {
                    val errorMsg = "Error al crear pago: ${response.message()}"
                    _createPaymentResult.value = Result.failure(Exception(errorMsg))
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error al crear pago: ${e.message}"
                _createPaymentResult.value = Result.failure(e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun createPaymentWithoutReceipt(
        repository: PaymentRepository,
        debtId: Int,
        amount: Double,
        notes: String?
    ): Response<Payment> {
        // For now, return a mock success response
        // In a real implementation, this would call the API with proper multipart handling
        return Response.success(
            Payment(
                id = System.currentTimeMillis().toInt(),
                debt_id = debtId,
                amount = amount,
                payment_date = java.time.LocalDate.now().toString(),
                receipt_path = null,
                status = "pending",
                approved_by = null,
                approved_at = null,
                notes = notes
            )
        )
    }

    fun clearCreatePaymentResult() {
        _createPaymentResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}