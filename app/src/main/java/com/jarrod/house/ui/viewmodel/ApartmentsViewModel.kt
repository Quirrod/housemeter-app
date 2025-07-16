package com.jarrod.house.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.data.model.Floor
import com.jarrod.house.data.repository.ApartmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ApartmentsViewModel : ViewModel() {
    private val _apartments = MutableStateFlow<List<Apartment>>(emptyList())
    val apartments: StateFlow<List<Apartment>> = _apartments

    private val _floors = MutableStateFlow<List<Floor>>(emptyList())
    val floors: StateFlow<List<Floor>> = _floors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _createApartmentResult = MutableStateFlow<Result<Apartment>?>(null)
    val createApartmentResult: StateFlow<Result<Apartment>?> = _createApartmentResult

    private val _createFloorResult = MutableStateFlow<Result<Floor>?>(null)
    val createFloorResult: StateFlow<Result<Floor>?> = _createFloorResult

    private val _updateApartmentResult = MutableStateFlow<Result<Boolean>?>(null)
    val updateApartmentResult: StateFlow<Result<Boolean>?> = _updateApartmentResult

    private val _updateFloorResult = MutableStateFlow<Result<Boolean>?>(null)
    val updateFloorResult: StateFlow<Result<Boolean>?> = _updateFloorResult

    fun loadApartments(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.getApartments()
                
                if (response.isSuccessful) {
                    _apartments.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar apartamentos: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFloors(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.getFloors()
                
                if (response.isSuccessful) {
                    _floors.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar pisos: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createApartment(
        context: Context,
        floorId: Int,
        apartmentNumber: String,
        meterNumber: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.createApartment(floorId, apartmentNumber, meterNumber)
                
                if (response.isSuccessful && response.body() != null) {
                    _createApartmentResult.value = Result.success(response.body()!!)
                    // Reload apartments to get updated list
                    loadApartments(context)
                } else {
                    val errorMsg = "Error al crear apartamento: ${response.message()}"
                    _createApartmentResult.value = Result.failure(Exception(errorMsg))
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                _createApartmentResult.value = Result.failure(e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createFloor(
        context: Context,
        floorNumber: Int,
        description: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.createFloor(floorNumber, description)
                
                if (response.isSuccessful && response.body() != null) {
                    _createFloorResult.value = Result.success(response.body()!!)
                    // Reload floors to get updated list
                    loadFloors(context)
                } else {
                    val errorMsg = "Error al crear piso: ${response.message()}"
                    _createFloorResult.value = Result.failure(Exception(errorMsg))
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                _createFloorResult.value = Result.failure(e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateApartment(
        context: Context,
        id: Int,
        floorId: Int,
        apartmentNumber: String,
        meterNumber: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.updateApartment(id, floorId, apartmentNumber, meterNumber)
                
                if (response.isSuccessful) {
                    _updateApartmentResult.value = Result.success(true)
                    // Reload apartments to get updated list
                    loadApartments(context)
                } else {
                    val errorMsg = "Error al actualizar apartamento: ${response.message()}"
                    _updateApartmentResult.value = Result.failure(Exception(errorMsg))
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                _updateApartmentResult.value = Result.failure(e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFloor(
        context: Context,
        id: Int,
        floorNumber: Int,
        description: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.updateFloor(id, floorNumber, description)
                
                if (response.isSuccessful) {
                    _updateFloorResult.value = Result.success(true)
                    // Reload floors to get updated list
                    loadFloors(context)
                } else {
                    val errorMsg = "Error al actualizar piso: ${response.message()}"
                    _updateFloorResult.value = Result.failure(Exception(errorMsg))
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                _updateFloorResult.value = Result.failure(e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteApartment(context: Context, apartmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.deleteApartment(apartmentId)
                
                if (response.isSuccessful) {
                    // Remove apartment from local list
                    _apartments.value = _apartments.value.filter { it.id != apartmentId }
                } else {
                    _error.value = "Error al eliminar apartamento: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFloor(context: Context, floorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = ApartmentRepository(context)
                val response = repository.deleteFloor(floorId)
                
                if (response.isSuccessful) {
                    // Remove floor from local list
                    _floors.value = _floors.value.filter { it.id != floorId }
                } else {
                    _error.value = "Error al eliminar piso: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCreateResults() {
        _createApartmentResult.value = null
        _createFloorResult.value = null
        _updateApartmentResult.value = null
        _updateFloorResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}