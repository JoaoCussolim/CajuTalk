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

    private val _updateUserResult = MutableLiveData<Result<UsuarioDto>?>()
    val updateUserResult: LiveData<Result<UsuarioDto>?> = _updateUserResult

    private val _deleteUserResult = MutableLiveData<Result<Unit>?>()
    val deleteUserResult: LiveData<Result<Unit>?> = _deleteUserResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun updateUserProfile(
        currentUserDto: UsuarioDto,
        nome: String,
        recado: String,
        newImageUri: Uri?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Passo 1: Determina a URL final da foto.
                val fotoUrlFinal = if (newImageUri != null) {
                    fileUploadRepository.uploadFile(newImageUri).getOrThrow().url
                } else {
                    currentUserDto.FotoPerfilURL
                }

                // Passo 2: Cria o DTO de atualização e envia para a API.
                val updateDto = UsuarioUpdateDto(
                    NomeUsuario = nome,
                    Recado = recado.ifBlank { null },
                    NovaFotoPerfil = fotoUrlFinal,
                    LoginUsuario = null,
                    SenhaUsuario = null
                )

                // Ignoramos a resposta da API, só verificamos se deu erro ou não.
                userRepository.updateUser(currentUserDto.ID, updateDto).getOrThrow()

                // Passo 3: SUCESSO! Construímos o DTO correto NÓS MESMOS.
                val newCorrectDto = currentUserDto.copy(
                    NomeUsuario = nome,
                    Recado = recado.ifBlank { null },
                    FotoPerfilURL = fotoUrlFinal
                )
                _updateUserResult.postValue(Result.success(newCorrectDto))

            } catch (e: Exception) {
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

    fun updateUser(userId: Int, updateDto: UsuarioUpdateDto): Result<Usuario> {
        throw NotImplementedError("Use updateUserProfile para o fluxo de UI. Esta função é para outros usos.")
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