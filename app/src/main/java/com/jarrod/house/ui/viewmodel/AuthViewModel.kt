package com.jarrod.house.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarrod.house.data.model.LoginResponse
import com.jarrod.house.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _loginResult = MutableSharedFlow<Result<LoginResponse>>()
    val loginResult: SharedFlow<Result<LoginResponse>> = _loginResult

    fun login(context: Context, username: String, password: String) {
        viewModelScope.launch {
            try {
                val repository = AuthRepository(context)
                val response = repository.login(username, password)
                
                if (response.isSuccessful && response.body() != null) {
                    _loginResult.emit(Result.success(response.body()!!))
                } else {
                    _loginResult.emit(Result.failure(Exception("Credenciales incorrectas")))
                }
            } catch (e: Exception) {
                _loginResult.emit(Result.failure(e))
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            val repository = AuthRepository(context)
            repository.logout()
        }
    }
}