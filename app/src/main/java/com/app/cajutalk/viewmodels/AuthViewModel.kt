// Em: com.app.cajutalk.viewmodels (ou seu pacote)
package com.app.cajutalk.viewmodels // Adapte o pacote

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.cajutalk.User // Seu modelo de usuário local
import com.app.cajutalk.mainUser // Sua variável global mainUser
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.RetrofitClient
import com.app.cajutalk.network.dto.LoginRequest
import com.app.cajutalk.network.dto.RegisterRequest
import com.app.cajutalk.network.dto.TokenResponse
import com.app.cajutalk.network.dto.UsuarioDto // DTO do Kotlin para dados do usuário
import com.auth0.android.jwt.JWT // Para decodificar JWT e pegar o ID do usuário
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Estado da UI para autenticação
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    // Sucesso agora apenas indica que a operação de token foi bem-sucedida
    // Os dados do usuário serão buscados separadamente se necessário
    data class Success(val tokenResponse: TokenResponse) : AuthUiState()
    data class UserProfileLoaded(val user: User) : AuthUiState() // Novo estado para usuário carregado
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AuthViewModel"
    private val apiService: ApiService = RetrofitClient.instance

    var authUiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    private val sharedPreferences = application.getSharedPreferences("CajuTalkPrefs", Context.MODE_PRIVATE)

    fun register(registerRequest: RegisterRequest) {
        authUiState = AuthUiState.Loading
        apiService.registerUser(registerRequest).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val tokenInfo = response.body()!!
                    // Registro bem-sucedido, o usuário agora precisa fazer login
                    // ou você pode tentar logar automaticamente.
                    // Por simplicidade, vamos apenas indicar sucesso no registro.
                    authUiState = AuthUiState.Success(tokenInfo) // Indica que o registro deu certo e temos tokens (embora possamos não usá-los diretamente aqui)
                    Log.d(TAG, "Registro bem-sucedido. AccessToken: ${tokenInfo.accessToken}")
                    Toast.makeText(getApplication(), "Cadastro realizado com sucesso! Faça o login.", Toast.LENGTH_LONG).show()
                } else {
                    val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Erro ao registrar: ${response.code()}"
                    authUiState = AuthUiState.Error(errorMsg)
                    Log.e(TAG, "Erro ao registrar: $errorMsg")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                authUiState = AuthUiState.Error("Falha na conexão: ${t.message}")
                Log.e(TAG, "Falha ao registrar: ${t.message}", t)
            }
        })
    }

    fun login(loginRequest: LoginRequest) {
        authUiState = AuthUiState.Loading
        apiService.loginUser(loginRequest).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val tokenInfo = response.body()!!
                    saveAuthTokens(tokenInfo)
                    authUiState = AuthUiState.Success(tokenInfo) // Transição para sucesso com tokens
                    // Agora, buscar os dados do usuário
                    fetchUserDetails(tokenInfo.accessToken)
                    Log.d(TAG, "Login (tokens obtidos) bem-sucedido. AccessToken: ${tokenInfo.accessToken}")
                } else {
                    val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Login ou senha inválidos: ${response.code()}"
                    authUiState = AuthUiState.Error(errorMsg)
                    Log.e(TAG, "Erro ao fazer login: $errorMsg")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                authUiState = AuthUiState.Error("Falha na conexão: ${t.message}")
                Log.e(TAG, "Falha ao fazer login: ${t.message}", t)
            }
        })
    }

    private fun fetchUserDetails(accessToken: String) {
        // Decodificar o JWT para pegar o User ID (NameIdentifier)
        // Adicione a dependência: implementation 'com.auth0.android:jwtdecode:2.0.2'
        val jwt = JWT(accessToken)
        val userId = jwt.getClaim("nameid").asString()?.toIntOrNull() // "nameid" é o ClaimTypes.NameIdentifier

        if (userId == null) {
            authUiState = AuthUiState.Error("Não foi possível obter o ID do usuário do token.")
            Log.e(TAG, "ID do usuário não encontrado no token JWT.")
            return
        }

        Log.d(TAG, "Buscando detalhes para o usuário ID: $userId com token: Bearer $accessToken")

        // Chamar API para buscar dados do usuário
        apiService.getUserById("Bearer $accessToken", userId).enqueue(object : Callback<UsuarioDto> {
            override fun onResponse(call: Call<UsuarioDto>, response: Response<UsuarioDto>) {
                if (response.isSuccessful && response.body() != null) {
                    val userDto = response.body()!!
                    val loggedInUser = User( // Mapeia para o seu modelo User local
                        id = userDto.id,
                        login = userDto.loginUsuario,
                        name = userDto.nomeUsuario ?: userDto.loginUsuario,
                        imageUrl = userDto.fotoPerfilURL,
                        senha = "" // Senha não é armazenada/usada após login
                    )
                    mainUser = loggedInUser // Atualiza o mainUser global
                    saveUserDetails(loggedInUser) // Salva no SharedPreferences
                    authUiState = AuthUiState.UserProfileLoaded(loggedInUser) // Estado final de sucesso
                    Log.d(TAG, "Detalhes do usuário carregados: ${loggedInUser.name}")
                } else {
                    val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Erro ao buscar dados do usuário: ${response.code()}"
                    authUiState = AuthUiState.Error(errorMsg)
                    Log.e(TAG, "Erro ao buscar dados do usuário: $errorMsg")
                }
            }

            override fun onFailure(call: Call<UsuarioDto>, t: Throwable) {
                authUiState = AuthUiState.Error("Falha ao buscar dados do usuário: ${t.message}")
                Log.e(TAG, "Falha ao buscar dados do usuário: ${t.message}", t)
            }
        })
    }


    private fun saveAuthTokens(tokenResponse: TokenResponse) {
        with(sharedPreferences.edit()) {
            putString("access_token", tokenResponse.accessToken)
            putString("refresh_token", tokenResponse.refreshToken)
            putString("access_token_expiration", tokenResponse.accessTokenExpiration) // Salvar expiração se quiser usar
            apply()
        }
    }

    private fun saveUserDetails(user: User) { // Salva seu modelo User
        with(sharedPreferences.edit()) {
            putInt("user_id", user.id)
            putString("user_login", user.login)
            putString("user_name", user.name)
            putString("user_image_url", user.imageUrl)
            apply()
        }
    }

    fun getAccessToken(): String? = sharedPreferences.getString("access_token", null)
    // fun getRefreshToken(): String? = sharedPreferences.getString("refresh_token", null) // Para lógica de refresh

    fun getSavedUser(): User? {
        val id = sharedPreferences.getInt("user_id", -1)
        if (id == -1) return null
        return User(
            id = id,
            login = sharedPreferences.getString("user_login", "") ?: "",
            name = sharedPreferences.getString("user_name", "") ?: "",
            imageUrl = sharedPreferences.getString("user_image_url", null),
            senha = "" // Senha não é armazenada
        )
    }

    fun logout() {
        with(sharedPreferences.edit()) {
            remove("access_token")
            remove("refresh_token")
            remove("access_token_expiration")
            remove("user_id")
            remove("user_login")
            remove("user_name")
            remove("user_image_url")
            apply()
        }
        mainUser = User(id = -1, login = "Visitante", senha = "", name = "Visitante", imageUrl = null) // Resetar mainUser
        authUiState = AuthUiState.Idle
        Log.d(TAG, "Usuário deslogado.")
    }

    fun resetAuthState() {
        authUiState = AuthUiState.Idle
    }

    // Helper para extrair mensagem de erro do JSON da API (se houver um campo "message")
    private fun String.extractMessage(): String {
        return try {
            val jsonObject = org.json.JSONObject(this)
            jsonObject.optString("Message", this) // Tenta pegar "Message", senão retorna a string original
        } catch (e: org.json.JSONException) {
            this // Se não for JSON válido, retorna a string original
        }
    }
}