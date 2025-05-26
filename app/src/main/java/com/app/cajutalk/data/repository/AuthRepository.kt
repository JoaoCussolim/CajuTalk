package com.app.cajutalk.data.repository

import android.content.Context
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.models.LoginModel
import com.app.cajutalk.network.models.RegisterModel
import com.app.cajutalk.network.models.TokenResponse
import com.app.cajutalk.network.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val applicationContext: Context
) {
    suspend fun login(loginModel: LoginModel): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(loginModel)
                if (response.isSuccessful && response.body() != null) {
                    val tokenResponse = response.body()!!
                    TokenManager.saveTokens(
                        applicationContext,
                        tokenResponse.AccessToken,
                        tokenResponse.RefreshToken
                    )
                    Result.success(tokenResponse)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Login falhou: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(registerModel: RegisterModel): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(registerModel)
                if (response.isSuccessful && response.body() != null) {
                    val tokenResponse = response.body()!!
                    TokenManager.saveTokens(
                        applicationContext,
                        tokenResponse.AccessToken,
                        tokenResponse.RefreshToken
                    )
                    Result.success(tokenResponse)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Cadastro falhou: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        TokenManager.clearTokens(applicationContext)
    }

    fun isLoggedIn(): Boolean {
        return TokenManager.getAccessToken(applicationContext) != null
    }

    fun getAccessToken(): String? {
        return TokenManager.getAccessToken(applicationContext)
    }

    fun getRefreshToken(): String? {
        return TokenManager.getRefreshToken(applicationContext)
    }
}