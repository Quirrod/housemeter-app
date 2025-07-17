package com.jarrod.house.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarrod.house.data.model.User
import com.jarrod.house.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _createResult = MutableStateFlow<Result<User>?>(null)
    val createResult: StateFlow<Result<User>?> = _createResult

    private val _updateResult = MutableStateFlow<Result<Boolean>?>(null)
    val updateResult: StateFlow<Result<Boolean>?> = _updateResult

    fun loadUsers(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = UserRepository(context)
                val response = repository.getUsers()
                
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar usuarios: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createUser(
        context: Context,
        username: String,
        password: String,
        role: String,
        apartmentId: Int?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = UserRepository(context)
                val response = repository.createUser(username, password, role, apartmentId)
                
                if (response.isSuccessful && response.body() != null) {
                    _createResult.value = Result.success(response.body()!!)
                    // Reload users to get updated list
                    loadUsers(context)
                } else {
                    val errorMsg = "Error al crear usuario: ${response.message()}"
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

    fun updateUser(
        context: Context,
        id: Int,
        username: String,
        password: String?,
        role: String,
        apartmentId: Int?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = UserRepository(context)
                val response = repository.updateUser(id, username, password, role, apartmentId)
                
                if (response.isSuccessful) {
                    _updateResult.value = Result.success(true)
                    // Reload users to get updated list
                    loadUsers(context)
                } else {
                    val errorMsg = "Error al actualizar usuario: ${response.message()}"
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

    fun deleteUser(context: Context, userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = UserRepository(context)
                val response = repository.deleteUser(userId)
                
                if (response.isSuccessful) {
                    // Remove user from local list
                    _users.value = _users.value.filter { it.id != userId }
                } else {
                    _error.value = "Error al eliminar usuario: ${response.message()}"
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
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}