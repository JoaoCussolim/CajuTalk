package com.app.cajutalk.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.cajutalk.data.repository.AuthRepository
import com.app.cajutalk.data.repository.MensagemRepository
import com.app.cajutalk.data.repository.SalaRepository
import com.app.cajutalk.data.repository.UserRepository
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.RetrofitClient

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val apiService = RetrofitClient.createService(application, ApiService::class.java)

        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val authRepository = AuthRepository(apiService, application.applicationContext)
                AuthViewModel(application, authRepository) as T
            }
            modelClass.isAssignableFrom(MensagemViewModel::class.java) -> {
                val mensagemRepository = MensagemRepository(apiService, application.applicationContext)
                MensagemViewModel(application, mensagemRepository) as T
            }
            modelClass.isAssignableFrom(SalaViewModel::class.java) -> {
                val salaRepository = SalaRepository(apiService)
                SalaViewModel(application, salaRepository) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                val userRepository = UserRepository(apiService, application.applicationContext)
                UserViewModel(application, userRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}