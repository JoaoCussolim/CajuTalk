package com.app.cajutalk.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.cajutalk.data.repository.FileUploadRepository
import com.app.cajutalk.data.repository.UserRepository
import com.app.cajutalk.network.models.UploadResponse
import com.app.cajutalk.network.models.Usuario
import com.app.cajutalk.network.models.UsuarioDto
import com.app.cajutalk.network.models.UsuarioUpdateDto
import kotlinx.coroutines.launch

class UserViewModel(application: Application, private val userRepository: UserRepository, private val fileUploadRepository: FileUploadRepository) : AndroidViewModel(application) { // Updated constructor

    private val _userById = MutableLiveData<Result<UsuarioDto>>()
    val userById: LiveData<Result<UsuarioDto>> = _userById

    private val _currentUserDetails = MutableLiveData<Result<UsuarioDto>>()
    val currentUserDetails: LiveData<Result<UsuarioDto>> = _currentUserDetails

    private val _allUsers = MutableLiveData<Result<List<UsuarioDto>>>()
    val allUsers: LiveData<Result<List<UsuarioDto>>> = _allUsers

    private val _searchedUsers = MutableLiveData<Result<List<UsuarioDto>>>()
    val searchedUsers: LiveData<Result<List<UsuarioDto>>> = _searchedUsers

    private val _updateUserResult = MutableLiveData<Result<Usuario>?>()
    val updateUserResult: LiveData<Result<Usuario>?> = _updateUserResult

    private val _deleteUserResult = MutableLiveData<Result<Unit>?>()
    val deleteUserResult: LiveData<Result<Unit>?> = _deleteUserResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun updateUserProfile(userId: Int, nome: String, recado: String, oldFotoUrl: String?, newImageUri: Uri?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fotoUrlFinal = if (newImageUri != null) {
                    val uploadResult = fileUploadRepository.uploadFile(newImageUri)
                    uploadResult.getOrNull()?.url ?: oldFotoUrl
                } else {
                    oldFotoUrl
                }

                val updateDto = UsuarioUpdateDto(
                    NomeUsuario = nome,
                    Recado = recado,
                    NovaFotoPerfil = fotoUrlFinal,
                    LoginUsuario = null,
                    SenhaUsuario = null
                )
                val finalResult = userRepository.updateUser(userId, updateDto)
                _updateUserResult.postValue(finalResult)

            } catch (e: Exception) {
                // MUDANÇA: Adicionado Log.e para capturar o erro completo
                Log.e("UserViewModel", "Falha ao atualizar o perfil", e)
                _updateUserResult.postValue(Result.failure(e))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun onDeleteUserResultConsumed() {
        _deleteUserResult.value = null
    }

    fun onUpdateUserResultConsumed() {
        _updateUserResult.value = null
    }

    fun getUserById(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.getUserById(userId)
            _userById.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun getCurrentUserDetails() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.getCurrentUserDetails()
            _currentUserDetails.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun getAllUsers() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.getAllUsers()
            _allUsers.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun searchUsers(searchTerm: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.searchUsers(searchTerm)
            _searchedUsers.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun updateUser(userId: Int, updateDto: UsuarioUpdateDto) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.updateUser(userId, updateDto)
            _updateUserResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun deleteUser(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.deleteUser(userId)
            _deleteUserResult.postValue(result)
            _isLoading.postValue(false)
        }
    }
}