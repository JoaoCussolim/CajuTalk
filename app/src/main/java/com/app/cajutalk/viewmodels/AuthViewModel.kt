package com.app.cajutalk.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.cajutalk.data.repository.AuthRepository
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.RetrofitClient
import com.app.cajutalk.network.models.LoginModel
import com.app.cajutalk.network.models.RegisterModel
import com.app.cajutalk.network.models.TokenResponse
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application)  {
    private val authRepository: AuthRepository

    init {
        val apiService = RetrofitClient.createService(application, ApiService::class.java)
        authRepository = AuthRepository(apiService, application.applicationContext)
    }

    private val _loginResult = MutableLiveData<Result<TokenResponse>>()
    val loginResult: LiveData<Result<TokenResponse>> = _loginResult

    private val _registerResult = MutableLiveData<Result<TokenResponse>>()
    val registerResult: LiveData<Result<TokenResponse>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(loginUsuario: String, senhaUsuario: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.login(LoginModel(loginUsuario, senhaUsuario))
            _loginResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun register(nomeUsuario: String, loginUsuario: String, senhaUsuario: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(RegisterModel(nomeUsuario, loginUsuario, senhaUsuario))
            _registerResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}