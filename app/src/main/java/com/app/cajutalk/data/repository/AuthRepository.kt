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
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null && tokenResponse.AccessToken != null && tokenResponse.RefreshToken != null) { // <--- Adição da verificação de null
                        TokenManager.saveTokens(
                            applicationContext,
                            tokenResponse.AccessToken,
                            tokenResponse.RefreshToken
                        )
                        Result.success(tokenResponse)
                    } else {
                        // Se o corpo ou os tokens são nulos apesar de isSuccessful
                        val errorMsg = "Resposta de token inválida do servidor: ${response.code()} - ${response.errorBody()?.string() ?: "Corpo nulo"}"
                        Result.failure(Exception(errorMsg))
                    }
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
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null && tokenResponse.AccessToken != null && tokenResponse.RefreshToken != null) { // <--- Adição da verificação de null
                        TokenManager.saveTokens(
                            applicationContext,
                            tokenResponse.AccessToken,
                            tokenResponse.RefreshToken
                        )
                        Result.success(tokenResponse)
                    } else {
                        val errorMsg = "Resposta de token inválida do servidor: ${response.code()} - ${response.errorBody()?.string() ?: "Corpo nulo"}"
                        Result.failure(Exception(errorMsg))
                    }
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