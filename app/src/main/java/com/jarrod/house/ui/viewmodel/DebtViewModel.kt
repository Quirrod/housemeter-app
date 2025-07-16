package com.jarrod.house.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarrod.house.data.model.Debt
import com.jarrod.house.data.repository.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DebtViewModel : ViewModel() {
    private val _debts = MutableStateFlow<List<Debt>>(emptyList())
    val debts: StateFlow<List<Debt>> = _debts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _createResult = MutableStateFlow<Result<Debt>?>(null)
    val createResult: StateFlow<Result<Debt>?> = _createResult

    private val _updateResult = MutableStateFlow<Result<Boolean>?>(null)
    val updateResult: StateFlow<Result<Boolean>?> = _updateResult

    fun loadDebts(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = DebtRepository(context)
                val response = repository.getDebts()
                
                if (response.isSuccessful) {
                    _debts.value = response.body() ?: emptyList()
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

    fun createDebt(
        context: Context,
        apartmentId: Int,
        amount: Double,
        description: String?,
        dueDate: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = DebtRepository(context)
                val response = repository.createDebt(apartmentId, amount, description, dueDate)
                
                if (response.isSuccessful && response.body() != null) {
                    _createResult.value = Result.success(response.body()!!)
                    // Reload debts to get updated list
                    loadDebts(context)
                } else {
                    val errorMsg = "Error al crear deuda: ${response.message()}"
                    _createResult.value = Result.failure(Exception(errorMsg))
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                _createResult.value = Result.failure(e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDebt(
        context: Context,
        id: Int,
        apartmentId: Int,
        amount: Double,
        description: String?,
        dueDate: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = DebtRepository(context)
                val response = repository.updateDebt(id, apartmentId, amount, description, dueDate)
                
                if (response.isSuccessful) {
                    _updateResult.value = Result.success(true)
                    // Reload debts to get updated list
                    loadDebts(context)
                } else {
                    val errorMsg = "Error al actualizar deuda: ${response.message()}"
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

    fun deleteDebt(context: Context, debtId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = DebtRepository(context)
                val response = repository.deleteDebt(debtId)
                
                if (response.isSuccessful) {
                    // Remove debt from local list
                    _debts.value = _debts.value.filter { it.id != debtId }
                } else {
                    _error.value = "Error al eliminar deuda: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCreateResult() {
        _createResult.value = null
        _updateResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}