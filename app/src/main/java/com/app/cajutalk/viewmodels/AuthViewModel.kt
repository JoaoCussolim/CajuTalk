// Em: com.app.cajutalk.viewmodels
package com.app.cajutalk.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
// import androidx.lifecycle.viewModelScope // Não usado diretamente com Callbacks
import com.app.cajutalk.User // Seu modelo de usuário local ATUALIZADO
import com.app.cajutalk.mainUser // Sua variável global mainUser
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.RetrofitClient
import com.app.cajutalk.network.dto.LoginRequest
import com.app.cajutalk.network.dto.RegisterRequest
import com.app.cajutalk.network.dto.TokenResponse
import com.app.cajutalk.network.dto.UsuarioDto
import com.app.cajutalk.network.dto.UsuarioUpdateRequest
import com.auth0.android.jwt.JWT
// import kotlinx.coroutines.launch // Não usado diretamente com Callbacks
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Estado da UI para autenticação
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    // Sucesso no login/refresh - contém tokens, mas perfil será carregado separadamente
    data class AuthSuccess(val tokenResponse: TokenResponse) : AuthUiState()
    data class UserProfileLoaded(val user: User) : AuthUiState()
    data class UserProfileUpdated(val user: User) : AuthUiState()
    data class UserDeleted(val oldUserId: Int) : AuthUiState() // Passar o ID do usuário deletado
    // RegistrationSuccess agora pode ser mais simples, ou pode conter o TokenResponse se você quiser auto-login
    object RegistrationSuccessState : AuthUiState() // Simplificado
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AuthViewModel"
    private val apiService: ApiService = RetrofitClient.instance

    var authUiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    private val sharedPreferences = application.getSharedPreferences("CajuTalkPrefs", Context.MODE_PRIVATE)

    // Chamar no init da MainActivity ou de um ViewModel de nível superior
    // para tentar carregar o usuário se o token ainda for válido.
    init {
        loadUserIfAuthenticated()
    }

    private fun loadUserIfAuthenticated() {
        val token = getAccessToken()
        if (token != null) {
            // Opcional: Verificar validade do token aqui antes de prosseguir
            // Se o token for válido, buscar detalhes do usuário
            Log.d(TAG, "Token encontrado na inicialização. Buscando detalhes do usuário.")
            fetchUserDetails(token) // Isso mudará o authUiState para Loading e depois UserProfileLoaded ou Error
        } else {
            Log.d(TAG, "Nenhum token encontrado na inicialização.")
            authUiState = AuthUiState.Idle // Garantir que está Idle se não houver token
        }
    }


    fun register(registerRequest: RegisterRequest) {
        authUiState = AuthUiState.Loading
        apiService.registerUser(registerRequest).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // Opcional: você poderia salvar os tokens e tentar logar automaticamente,
                    // mas a prática comum é pedir para o usuário logar após o registro.
                    // val tokenInfo = response.body()!!
                    // saveAuthTokens(tokenInfo)
                    // authUiState = AuthUiState.AuthSuccess(tokenInfo)
                    // fetchUserDetails(tokenInfo.accessToken) // Se quisesse auto-login
                    authUiState = AuthUiState.RegistrationSuccessState // Estado simplificado para sucesso no registro
                    Log.d(TAG, "Registro bem-sucedido.")
                    Toast.makeText(getApplication(), "Cadastro realizado com sucesso! Faça o login.", Toast.LENGTH_LONG).show()
                } else {
                    val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Erro ao registrar: ${response.code()}"
                    authUiState = AuthUiState.Error(errorMsg)
                    Log.e(TAG, "Erro ao registrar: $errorMsg")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                authUiState = AuthUiState.Error("Falha na conexão ao registrar: ${t.message}")
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
                    // O estado de AuthSuccess é útil se a UI precisa reagir à obtenção de tokens
                    // antes do perfil do usuário ser totalmente carregado.
                    authUiState = AuthUiState.AuthSuccess(tokenInfo)
                    fetchUserDetails(tokenInfo.accessToken) // Buscar detalhes do usuário após obter tokens
                    Log.d(TAG, "Login (obtenção de tokens) bem-sucedido. AccessToken: ${tokenInfo.accessToken}")
                } else {
                    val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Login ou senha inválidos." // Mensagem mais genérica
                    authUiState = AuthUiState.Error(errorMsg)
                    Log.e(TAG, "Erro ao fazer login: Código ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                authUiState = AuthUiState.Error("Falha na conexão ao fazer login: ${t.message}")
                Log.e(TAG, "Falha ao fazer login: ${t.message}", t)
            }
        })
    }

    private fun fetchUserDetails(accessToken: String) {
        authUiState = AuthUiState.Loading // Indicar carregamento do perfil
        val jwt = JWT(accessToken)
        val userId = jwt.getClaim("nameid").asString()?.toIntOrNull()

        if (userId == null) {
            Log.e(TAG, "ID do usuário não encontrado no token JWT.")
            authUiState = AuthUiState.Error("Sessão inválida (ID não encontrado no token). Faça login novamente.")
            clearAuthData() // Limpar tokens inválidos
            return
        }

        Log.d(TAG, "Buscando detalhes para o usuário ID: $userId")
        // A API obterUsuarioPorId não requer o token no header na sua ApiService.kt atual.
        // Se requerer, adicione apiService.obterUsuarioPorId("Bearer $accessToken", userId)
        apiService.obterUsuarioPorId(userId = userId).enqueue(object : Callback<UsuarioDto> {
            override fun onResponse(call: Call<UsuarioDto>, response: Response<UsuarioDto>) {
                if (response.isSuccessful && response.body() != null) {
                    val userDto = response.body()!!
                    val loggedInUser = User(userDto) // Usar o construtor que aceita UsuarioDto

                    mainUser = loggedInUser // Atualiza o mainUser global (considere remover dependência disso)
                    saveUserDetails(loggedInUser)
                    authUiState = AuthUiState.UserProfileLoaded(loggedInUser)
                    Log.d(TAG, "Detalhes do usuário carregados: ${loggedInUser.name}")
                } else {
                    val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Erro ao buscar dados do usuário: ${response.code()}"
                    Log.e(TAG, "Erro ao buscar dados do usuário: $errorMsg")
                    authUiState = AuthUiState.Error(errorMsg)
                    // Se não conseguir buscar os detalhes, talvez o token seja velho ou usuário deletado.
                    // Considere limpar os tokens se o erro for persistente ou um 401/404 aqui.
                    // clearAuthData()
                    // authUiState = AuthUiState.Error("Sua sessão pode ter expirado. Faça login novamente.")
                }
            }

            override fun onFailure(call: Call<UsuarioDto>, t: Throwable) {
                Log.e(TAG, "Falha na conexão ao buscar dados do usuário: ${t.message}", t)
                authUiState = AuthUiState.Error("Falha na conexão ao buscar dados do usuário: ${t.message}")
            }
        })
    }

    fun updateUserProfile(updateRequest: UsuarioUpdateRequest) {
        val token = getAccessToken()
        val currentSavedUser = getSavedUser()

        if (token == null || currentSavedUser == null) {
            authUiState = AuthUiState.Error("Usuário não autenticado para atualizar perfil.")
            return
        }

        authUiState = AuthUiState.Loading
        Log.d(TAG, "Atualizando perfil para usuário ID: ${currentSavedUser.id}")

        apiService.atualizarUsuario(token = "Bearer $token", userId = currentSavedUser.id, request = updateRequest)
            .enqueue(object : Callback<UsuarioDto> {
                override fun onResponse(call: Call<UsuarioDto>, response: Response<UsuarioDto>) {
                    if (response.isSuccessful && response.body() != null) {
                        val updatedUserDto = response.body()!!
                        val updatedUser = User(updatedUserDto) // Usar o construtor que aceita UsuarioDto

                        mainUser = updatedUser
                        saveUserDetails(updatedUser)
                        authUiState = AuthUiState.UserProfileUpdated(updatedUser)
                        // O Toast pode ser mostrado na UI observando o estado UserProfileUpdated
                        // Toast.makeText(getApplication(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Perfil do usuário atualizado: ${updatedUser.name}")
                    } else {
                        val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Erro ao atualizar perfil: ${response.code()}"
                        authUiState = AuthUiState.Error(errorMsg)
                        Log.e(TAG, "Erro ao atualizar perfil: $errorMsg")
                    }
                }

                override fun onFailure(call: Call<UsuarioDto>, t: Throwable) {
                    authUiState = AuthUiState.Error("Falha na conexão ao atualizar perfil: ${t.message}")
                    Log.e(TAG, "Falha ao atualizar perfil: ${t.message}", t)
                }
            })
    }

    fun deleteCurrentUserAccount() {
        val token = getAccessToken()
        val currentSavedUser = getSavedUser()


        if (token == null || currentSavedUser == null) {
            authUiState = AuthUiState.Error("Usuário não autenticado para deletar conta.")
            return
        }
        val userIdToDelete = currentSavedUser.id // Guardar o ID antes do logout
        authUiState = AuthUiState.Loading
        Log.d(TAG, "Deletando conta para usuário ID: $userIdToDelete")

        apiService.deletarUsuario(token = "Bearer $token", userId = userIdToDelete)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Usuário $userIdToDelete deletado com sucesso da API.")
                        clearAuthData() // Limpar dados locais e tokens
                        mainUser = User(id = -1, login = "Visitante", name = "Visitante", imageUrl = null, message = "", corFundoRGB = null)
                        authUiState = AuthUiState.UserDeleted(userIdToDelete) // Emitir estado de sucesso na deleção
                        // O Toast pode ser mostrado na UI
                        // Toast.makeText(getApplication(), "Conta deletada com sucesso.", Toast.LENGTH_LONG).show()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = errorBody?.extractMessage() ?: "Erro ao deletar conta: ${response.code()}"
                        Log.e(TAG, "Erro ao deletar conta: $errorMsg | Body: $errorBody")
                        if (response.code() == 409) { // Conflict
                            authUiState = AuthUiState.Error("Não foi possível deletar a conta. Pode haver dados associados (ex: salas criadas).")
                        } else {
                            authUiState = AuthUiState.Error(errorMsg)
                        }
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    authUiState = AuthUiState.Error("Falha na conexão ao deletar conta: ${t.message}")
                    Log.e(TAG, "Falha ao deletar conta: ${t.message}", t)
                }
            })
    }

    private fun saveAuthTokens(tokenResponse: TokenResponse) {
        with(sharedPreferences.edit()) {
            putString("access_token", tokenResponse.accessToken)
            putString("refresh_token", tokenResponse.refreshToken)
            putString("access_token_expiration", tokenResponse.accessTokenExpiration)
            apply()
        }
        Log.d(TAG, "Tokens salvos nas SharedPreferences.")
    }

    // Atualizado para salvar todos os campos relevantes do User
    private fun saveUserDetails(user: User) {
        with(sharedPreferences.edit()) {
            putInt("user_id", user.id)
            putString("user_login", user.login)
            putString("user_name", user.name)
            putString("user_image_url", user.imageUrl)
            putString("user_recado", user.message) // Salvar recado
            putString("user_cor_fundo_rgb", user.corFundoRGB) // Salvar cor de fundo
            apply()
        }
        Log.d(TAG, "Detalhes do usuário salvos: ${user.name}")
    }

    fun getAccessToken(): String? = sharedPreferences.getString("access_token", null)

    // Atualizado para carregar todos os campos relevantes do User
    fun getSavedUser(): User? {
        val id = sharedPreferences.getInt("user_id", -1)
        if (id == -1) return null // Se não há ID, não há usuário salvo

        return User(
            id = id,
            login = sharedPreferences.getString("user_login", "") ?: "",
            name = sharedPreferences.getString("user_name", "") ?: "",
            imageUrl = sharedPreferences.getString("user_image_url", null),
            /* recado = sharedPreferences.getString("user_recado", null), */
            corFundoRGB = sharedPreferences.getString("user_cor_fundo_rgb", null) // Carregar cor de fundo
        )
    }

    // Função para limpar todos os dados de autenticação e usuário
    private fun clearAuthData() {
        with(sharedPreferences.edit()) {
            remove("access_token")
            remove("refresh_token")
            remove("access_token_expiration")
            remove("user_id")
            remove("user_login")
            remove("user_name")
            remove("user_image_url")
            remove("user_recado")
            remove("user_cor_fundo_rgb")
            apply()
        }
        Log.d(TAG, "Dados de autenticação e usuário limpos das SharedPreferences.")
    }

    fun logout() {
        val currentId = getSavedUser()?.id ?: -1
        clearAuthData()
        mainUser = User(id = -1, login = "Visitante", name = "Visitante", imageUrl = null, message = "", corFundoRGB = null)
        authUiState = AuthUiState.UserDeleted(currentId) // Ou um AuthUiState.LoggedOut específico
        Log.d(TAG, "Usuário deslogado.")
    }

    fun resetAuthState() {
        // Se o usuário ainda estiver salvo nas SharedPreferences, recarregue-o
        // Caso contrário, vá para Idle. Isso evita que resetar o estado após um erro
        // faça o app pensar que o usuário deslogou se os tokens ainda estão lá.
        val savedUser = getSavedUser()
        if (getAccessToken() != null && savedUser != null) {
            authUiState = AuthUiState.UserProfileLoaded(savedUser)
        } else {
            authUiState = AuthUiState.Idle
        }
    }

    private fun String.extractMessage(): String {
        return try {
            // Melhorar a extração para lidar com diferentes formatos de erro da API
            val jsonObject = org.json.JSONObject(this)
            // Tentar campos comuns de mensagem de erro
            jsonObject.optString("message", jsonObject.optString("Message", jsonObject.optString("detail", this)))
        } catch (e: org.json.JSONException) {
            this
        }
    }
}