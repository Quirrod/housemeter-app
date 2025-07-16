package com.jarrod.house.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarrod.house.data.model.Payment
import com.jarrod.house.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _updateResult = MutableStateFlow<Result<Boolean>?>(null)
    val updateResult: StateFlow<Result<Boolean>?> = _updateResult

    fun loadPayments(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = PaymentRepository(context)
                val response = repository.getPayments()
                
                if (response.isSuccessful) {
                    _payments.value = response.body() ?: emptyList()
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

    fun updatePaymentStatus(
        context: Context,
        paymentId: Int,
        status: String,
        notes: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = PaymentRepository(context)
                val response = repository.updatePaymentStatus(paymentId, status, notes)
                
                if (response.isSuccessful) {
                    _updateResult.value = Result.success(true)
                    // Reload payments to get updated list
                    loadPayments(context)
                } else {
                    val errorMsg = "Error al actualizar pago: ${response.message()}"
                    _updateResult.value = Result.failure(Exception(errorMsg))
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                _updateResult.value = Result.failure(e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePayment(context: Context, paymentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = PaymentRepository(context)
                val response = repository.deletePayment(paymentId)
                
                if (response.isSuccessful) {
                    // Remove payment from local list
                    _payments.value = _payments.value.filter { it.id != paymentId }
                } else {
                    _error.value = "Error al eliminar pago: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}