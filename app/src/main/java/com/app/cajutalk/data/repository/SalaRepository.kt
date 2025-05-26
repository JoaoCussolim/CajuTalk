package com.app.cajutalk.data.repository

import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.models.EntrarSalaDto
import com.app.cajutalk.network.models.SalaChatDto
import com.app.cajutalk.network.models.SalaCreateDto
import com.app.cajutalk.network.models.UsuarioDaSalaDto
import com.app.cajutalk.network.models.UsuarioSalaDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SalaRepository(
    private val apiService: ApiService
) {
    suspend fun createSala(salaCreateDto: SalaCreateDto): Result<SalaChatDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.criarSala(salaCreateDto) // Espera 201 Created
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao criar sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSalaById(salaId: Int): Result<SalaChatDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obterSalaPorId(salaId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getAllSalas(): Result<List<SalaChatDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obterTodasSalas()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar todas as salas: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteSala(salaId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deletarSala(salaId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao deletar sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUsersInSala(salaId: Int): Result<List<UsuarioDaSalaDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUsuariosDaSala(salaId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar usuários da sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun entrarSala(entrarSalaDto: EntrarSalaDto): Result<UsuarioSalaDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.entrarSala(entrarSalaDto)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao entrar na sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun sairSala(salaId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.sairSala(salaId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao sair da sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun banirUsuario(salaId: Int, usuarioIdParaBanir: Int): Result<UsuarioSalaDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.banirUsuario(salaId, usuarioIdParaBanir)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao banir usuário: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun desbanirUsuario(salaId: Int, usuarioIdParaDesbanir: Int): Result<UsuarioSalaDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.desbanirUsuario(salaId, usuarioIdParaDesbanir)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao desbanir usuário: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getRelacaoUsuarioSala(salaId: Int, usuarioId: Int): Result<UsuarioSalaDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRelacaoUsuarioSala(salaId, usuarioId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar relação usuário-sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}