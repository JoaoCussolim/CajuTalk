package com.app.cajutalk.data.repository

import android.content.Context
import com.auth0.android.jwt.JWT // Para decodificar o JWT e obter o ID do usuário
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.models.Usuario // A API retorna a entidade Usuario completa no update
import com.app.cajutalk.network.models.UsuarioDto
import com.app.cajutalk.network.models.UsuarioUpdateDto
import com.app.cajutalk.network.util.TokenManager // Para pegar o token e decodificar o ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService,
    private val applicationContext: Context // Para o TokenManager
) {
    suspend fun getUserById(userId: Int): Result<UsuarioDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obterUsuarioPorId(userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar usuário: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCurrentUserDetails(): Result<UsuarioDto> {
        val token = TokenManager.getAccessToken(applicationContext)
        if (token == null) {
            return Result.failure(Exception("Usuário não logado (sem token de acesso)."))
        }

        return try {
            val jwt = JWT(token)
            val userIdString = jwt.getClaim("nameid").asString() // "nameid" é o claim padrão para ID do usuário
            if (userIdString == null) {
                Result.failure(Exception("ID do usuário não encontrado no token."))
            } else {
                val userId = userIdString.toIntOrNull()
                if (userId == null) {
                    Result.failure(Exception("Formato inválido do ID do usuário no token."))
                } else {
                    getUserById(userId)
                }
            }
        } catch (e: Exception) {
            // Exceção ao decodificar o token ou converter o ID
            Result.failure(Exception("Erro ao processar token: ${e.message}"))
        }
    }

    suspend fun getAllUsers(): Result<List<UsuarioDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obterTodosUsuarios()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar todos os usuários: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchUsers(searchTerm: String): Result<List<UsuarioDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.buscarUsuarios(searchTerm)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar usuários: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUser(userId: Int, updateDto: UsuarioUpdateDto): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.atualizarUsuario(userId, updateDto)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao atualizar usuário: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteUser(userId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deletarUsuario(userId)
                if (response.isSuccessful) { // 204 No Content é isSuccessful
                    Result.success(Unit)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao deletar usuário: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}