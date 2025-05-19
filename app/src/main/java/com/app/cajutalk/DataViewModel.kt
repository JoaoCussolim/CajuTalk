// Em: com.app.cajutalk.viewmodels
package com.app.cajutalk.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cajutalk.Sala // Seu modelo de Sala local
import com.app.cajutalk.User // Seu modelo de User local
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.RetrofitClient
import com.app.cajutalk.network.dto.* // Importar todos os DTOs relevantes
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

// --- Estados da UI para DataViewModel ---
sealed class SalaUiState {
    object Idle : SalaUiState()
    object Loading : SalaUiState()
    data class SuccessList(val salas: List<SalaChatResponse>) : SalaUiState()
    data class SuccessSingle(val sala: SalaChatResponse) : SalaUiState()
    data class SuccessSalaCreated(val sala: SalaChatResponse) : SalaUiState()
    data class SuccessSalaDeleted(val response: Boolean) : SalaUiState()
    data class Error(val message: String) : SalaUiState()
}

sealed class UsuarioSalaUiState {
    object Idle : UsuarioSalaUiState()
    object Loading : UsuarioSalaUiState()
    data class SuccessJoined(val relacao: UsuarioSalaResponse) : UsuarioSalaUiState()
    data class SuccessLeft(val response: Boolean) : UsuarioSalaUiState()
    data class SuccessUserList(val usuarios: List<UsuarioDaSalaResponse>) : UsuarioSalaUiState()
    data class SuccessUserStatusChanged(val relacao: UsuarioSalaResponse) : UsuarioSalaUiState() // Ban/Unban
    data class Error(val message: String) : UsuarioSalaUiState()
}

sealed class MensagemUiState {
    object Idle : MensagemUiState()
    object Loading : MensagemUiState()
    data class SuccessSent(val mensagem: MensagemDto) : MensagemUiState()
    data class SuccessReceivedList(val mensagens: List<MensagemDto>) : MensagemUiState()
    data class Error(val message: String) : MensagemUiState()
}

sealed class UserSearchUiState {
    object Idle : UserSearchUiState()
    object Loading : UserSearchUiState()
    data class Success(val user: UsuarioDto) : UserSearchUiState()
    object NotFound : UserSearchUiState()
    data class Error(val message: String) : UserSearchUiState()
}


// DataViewModel agora é AndroidViewModel para acessar AuthViewModel e SharedPreferences se necessário
class DataViewModel(application: Application, private val authViewModel: AuthViewModel) : AndroidViewModel(application) {
    private val TAG = "DataViewModel"
    private val apiService: ApiService = RetrofitClient.instance

    // Estados da UI para diferentes operações
    var salaListUiState by mutableStateOf<SalaUiState>(SalaUiState.Idle)
        private set
    var activeSalaDetailUiState by mutableStateOf<SalaUiState>(SalaUiState.Idle) // Para detalhes de uma sala
        private set
    var usuarioSalaUiState by mutableStateOf<UsuarioSalaUiState>(UsuarioSalaUiState.Idle)
        private set
    var mensagemUiState by mutableStateOf<MensagemUiState>(MensagemUiState.Idle)
        private set
    var userSearchUiState by mutableStateOf<UserSearchUiState>(UserSearchUiState.Idle)
        private set
    var userSearchUiStateResult by mutableStateOf<UserSearchUiState>(UserSearchUiState.Idle)
        private set
    var usuarioProcurado by mutableStateOf<User?>(null)

    // --- Operações de Sala ---

    fun fetchAllSalas() {
        salaListUiState = SalaUiState.Loading
        apiService.obterTodasSalas().enqueue(object : Callback<List<SalaChatResponse>> {
            override fun onResponse(call: Call<List<SalaChatResponse>>, response: Response<List<SalaChatResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    salaListUiState = SalaUiState.SuccessList(response.body()!!)
                } else {
                    salaListUiState = SalaUiState.Error("Erro ao buscar salas: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<SalaChatResponse>>, t: Throwable) {
                salaListUiState = SalaUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun fetchSalaById(salaId: Int) {
        activeSalaDetailUiState = SalaUiState.Loading
        apiService.obterSalaPorId(salaId).enqueue(object : Callback<SalaChatResponse> {
            override fun onResponse(call: Call<SalaChatResponse>, response: Response<SalaChatResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    activeSalaDetailUiState = SalaUiState.SuccessSingle(response.body()!!)
                } else {
                    activeSalaDetailUiState = SalaUiState.Error("Erro ao buscar detalhes da sala: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<SalaChatResponse>, t: Throwable) {
                activeSalaDetailUiState = SalaUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun createSala(salaCreateRequest: SalaCreateRequest) {
        val token = authViewModel.getAccessToken() ?: return Unit.also {
            activeSalaDetailUiState = SalaUiState.Error("Não autenticado para criar sala.")
        }
        activeSalaDetailUiState = SalaUiState.Loading // Ou um estado específico de criação
        apiService.criarSala("Bearer $token", salaCreateRequest).enqueue(object : Callback<SalaChatResponse> {
            override fun onResponse(call: Call<SalaChatResponse>, response: Response<SalaChatResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    activeSalaDetailUiState = SalaUiState.SuccessSalaCreated(response.body()!!)
                    fetchAllSalas() // Atualizar lista de salas
                    Toast.makeText(getApplication(), "Sala '${response.body()!!.nome}' criada!", Toast.LENGTH_SHORT).show()
                } else {
                    activeSalaDetailUiState = SalaUiState.Error("Erro ao criar sala: ${response.code()} - ${response.errorBody()?.string()?.extractMessage()}")
                }
            }
            override fun onFailure(call: Call<SalaChatResponse>, t: Throwable) {
                activeSalaDetailUiState = SalaUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun deleteSala(salaId: Int) {
        val token = authViewModel.getAccessToken() ?: return Unit.also {
            activeSalaDetailUiState = SalaUiState.Error("Não autenticado para deletar sala.")
        }
        activeSalaDetailUiState = SalaUiState.Loading
        apiService.deletarSala("Bearer $token", salaId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    activeSalaDetailUiState = SalaUiState.SuccessSalaDeleted(response.isSuccessful)
                    fetchAllSalas() // Atualizar lista
                    Toast.makeText(getApplication(), "Sala deletada.", Toast.LENGTH_SHORT).show()
                } else {
                    activeSalaDetailUiState = SalaUiState.Error("Erro ao deletar sala: ${response.code()} - ${response.errorBody()?.string()?.extractMessage()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                activeSalaDetailUiState = SalaUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    // --- Operações de Usuário em Sala ---

    fun entrarSala(entrarSalaRequest: EntrarSalaRequest) {
        val token = authViewModel.getAccessToken() ?: return Unit.also {
            usuarioSalaUiState = UsuarioSalaUiState.Error("Não autenticado para entrar na sala.")
        }
        usuarioSalaUiState = UsuarioSalaUiState.Loading
        apiService.entrarSala("Bearer $token", entrarSalaRequest).enqueue(object: Callback<UsuarioSalaResponse>{
            override fun onResponse(call: Call<UsuarioSalaResponse>,response: Response<UsuarioSalaResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    usuarioSalaUiState = UsuarioSalaUiState.SuccessJoined(response.body()!!)
                    Toast.makeText(getApplication(), "Entrou na sala!", Toast.LENGTH_SHORT).show()
                    // Pode querer buscar usuários da sala aqui
                } else {
                    val errorMsg = response.errorBody()?.string()?.extractMessage() ?: "Erro ao entrar na sala: ${response.code()}"
                    usuarioSalaUiState = UsuarioSalaUiState.Error(errorMsg)
                }
            }
            override fun onFailure(call: Call<UsuarioSalaResponse>, t: Throwable) {
                usuarioSalaUiState = UsuarioSalaUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun sairSala(salaId: Int) {
        val token = authViewModel.getAccessToken() ?: return Unit.also {
            usuarioSalaUiState = UsuarioSalaUiState.Error("Não autenticado para sair da sala.")
        }
        usuarioSalaUiState = UsuarioSalaUiState.Loading
        apiService.sairSala("Bearer $token", salaId).enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    usuarioSalaUiState = UsuarioSalaUiState.SuccessLeft(response.isSuccessful)
                    Toast.makeText(getApplication(), "Saiu da sala.", Toast.LENGTH_SHORT).show()
                } else {
                    usuarioSalaUiState = UsuarioSalaUiState.Error("Erro ao sair da sala: ${response.code()} - ${response.errorBody()?.string()?.extractMessage()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                usuarioSalaUiState = UsuarioSalaUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun fetchUsuariosDaSala(salaId: Int) {
        val token = authViewModel.getAccessToken() ?: return Unit.also {
            usuarioSalaUiState = UsuarioSalaUiState.Error("Não autenticado.")
        }
        usuarioSalaUiState = UsuarioSalaUiState.Loading
        apiService.getUsuariosDaSala("Bearer $token", salaId).enqueue(object: Callback<List<UsuarioDaSalaResponse>>{
            override fun onResponse(call: Call<List<UsuarioDaSalaResponse>>, response: Response<List<UsuarioDaSalaResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    usuarioSalaUiState = UsuarioSalaUiState.SuccessUserList(response.body()!!)
                } else {
                    usuarioSalaUiState = UsuarioSalaUiState.Error("Erro ao buscar usuários da sala: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<UsuarioDaSalaResponse>>, t: Throwable) {
                usuarioSalaUiState = UsuarioSalaUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun banirUsuarioDaSala(salaId: Int, usuarioIdParaBanir: Int) {
        val token = authViewModel.getAccessToken() ?: return
        usuarioSalaUiState = UsuarioSalaUiState.Loading
        apiService.banirUsuario("Bearer $token", salaId, usuarioIdParaBanir).enqueue(object : Callback<UsuarioSalaResponse>{
            override fun onResponse(call: Call<UsuarioSalaResponse>,response: Response<UsuarioSalaResponse>) {
                if(response.isSuccessful && response.body() != null){
                    usuarioSalaUiState = UsuarioSalaUiState.SuccessUserStatusChanged(response.body()!!)
                    fetchUsuariosDaSala(salaId) // Refresh list
                    Toast.makeText(getApplication(), "Usuário banido.", Toast.LENGTH_SHORT).show()
                } else {
                    usuarioSalaUiState = UsuarioSalaUiState.Error("Erro ao banir: ${response.errorBody()?.string()?.extractMessage()}")
                }
            }
            override fun onFailure(call: Call<UsuarioSalaResponse>, t: Throwable) {
                usuarioSalaUiState = UsuarioSalaUiState.Error("Falha ao banir: ${t.message}")
            }
        })
    }

    fun desbanirUsuarioDaSala(salaId: Int, usuarioIdParaDesbanir: Int) {
        val token = authViewModel.getAccessToken() ?: return
        usuarioSalaUiState = UsuarioSalaUiState.Loading
        apiService.desbanirUsuario("Bearer $token", salaId, usuarioIdParaDesbanir).enqueue(object : Callback<UsuarioSalaResponse>{
            override fun onResponse(call: Call<UsuarioSalaResponse>,response: Response<UsuarioSalaResponse>) {
                if(response.isSuccessful && response.body() != null){
                    usuarioSalaUiState = UsuarioSalaUiState.SuccessUserStatusChanged(response.body()!!)
                    fetchUsuariosDaSala(salaId) // Refresh list
                    Toast.makeText(getApplication(), "Usuário desbanido.", Toast.LENGTH_SHORT).show()
                } else {
                    usuarioSalaUiState = UsuarioSalaUiState.Error("Erro ao desbanir: ${response.errorBody()?.string()?.extractMessage()}")
                }
            }
            override fun onFailure(call: Call<UsuarioSalaResponse>, t: Throwable) {
                usuarioSalaUiState = UsuarioSalaUiState.Error("Falha ao desbanir: ${t.message}")
            }
        })
    }


    // --- Operações de Mensagem ---
    fun fetchMensagensDaSala(salaId: Int) {
        val token = authViewModel.getAccessToken() ?: return Unit.also {
            mensagemUiState = MensagemUiState.Error("Não autenticado.")
        }
        mensagemUiState = MensagemUiState.Loading
        apiService.obterMensagensPorSala("Bearer $token", salaId).enqueue(object: Callback<List<MensagemDto>>{
            override fun onResponse(call: Call<List<MensagemDto>>, response: Response<List<MensagemDto>>) {
                if (response.isSuccessful && response.body() != null) {
                    mensagemUiState = MensagemUiState.SuccessReceivedList(response.body()!!)
                } else {
                    mensagemUiState = MensagemUiState.Error("Erro ao buscar mensagens: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<MensagemDto>>, t: Throwable) {
                mensagemUiState = MensagemUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun enviarMensagemTexto(salaId: Int, conteudo: String) {
        val token = authViewModel.getAccessToken() ?: return
        val idSalaBody = salaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val conteudoBody = conteudo.toRequestBody("text/plain".toMediaTypeOrNull())
        val tipoMensagemBody = "Texto".toRequestBody("text/plain".toMediaTypeOrNull()) // Ou TipoMensagemEnum.Texto.name

        mensagemUiState = MensagemUiState.Loading
        apiService.enviarMensagem(
            token = "Bearer $token",
            idSala = idSalaBody,
            conteudo = conteudoBody,
            tipoMensagem = tipoMensagemBody,
            arquivo = null // Sem arquivo para mensagem de texto
        ).enqueue(object: Callback<MensagemDto>{
            override fun onResponse(call: Call<MensagemDto>, response: Response<MensagemDto>) {
                if (response.isSuccessful && response.body() != null) {
                    mensagemUiState = MensagemUiState.SuccessSent(response.body()!!)
                    fetchMensagensDaSala(salaId) // Recarregar mensagens
                } else {
                    mensagemUiState = MensagemUiState.Error("Erro ao enviar mensagem: ${response.code()} - ${response.errorBody()?.string()?.extractMessage()}")
                }
            }
            override fun onFailure(call: Call<MensagemDto>, t: Throwable) {
                mensagemUiState = MensagemUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun enviarMensagemComArquivo(salaId: Int, conteudoTextoOpcional: String?, arquivo: File, tipoMensagemApi: String) {
        val token = authViewModel.getAccessToken() ?: return

        val idSalaBody = salaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        // O conteúdo da mensagem na API C# será a URL do arquivo se houver arquivo.
        // Se houver texto E arquivo, a API parece usar a URL do arquivo como conteúdo.
        // Por simplicidade, se houver arquivo, o texto aqui é ignorado ou pode ser usado para metadados se a API suportar.
        // Vamos assumir que a API C# espera que o campo "Conteudo" seja a URL do arquivo se um arquivo for enviado.
        // Portanto, o RequestBody para "Conteudo" será o nome do arquivo ou um placeholder,
        // já que a API C# define o `conteudoMensagem = arquivoUrl;`
        val conteudoParaApi = arquivo.name // Ou um placeholder, ou conteudoTextoOpcional se a API tratar isso
        val conteudoBody = conteudoParaApi.toRequestBody("text/plain".toMediaTypeOrNull())
        val tipoMensagemBody = tipoMensagemApi.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = arquivo.asRequestBody("application/octet-stream".toMediaTypeOrNull()) // Use o MIME type correto
        val bodyArquivo = MultipartBody.Part.createFormData("arquivo", arquivo.name, requestFile)

        mensagemUiState = MensagemUiState.Loading
        apiService.enviarMensagem(
            token = "Bearer $token",
            idSala = idSalaBody,
            conteudo = conteudoBody, // Este será sobrescrito pela URL do arquivo na API
            tipoMensagem = tipoMensagemBody,
            arquivo = bodyArquivo
        ).enqueue(object: Callback<MensagemDto>{
            override fun onResponse(call: Call<MensagemDto>, response: Response<MensagemDto>) {
                if (response.isSuccessful && response.body() != null) {
                    mensagemUiState = MensagemUiState.SuccessSent(response.body()!!)
                    fetchMensagensDaSala(salaId) // Recarregar mensagens
                } else {
                    mensagemUiState = MensagemUiState.Error("Erro ao enviar arquivo: ${response.code()} - ${response.errorBody()?.string()?.extractMessage()}")
                }
            }
            override fun onFailure(call: Call<MensagemDto>, t: Throwable) {
                mensagemUiState = MensagemUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    // --- Busca de Usuário (funcionalidade original do DataViewModel) ---
    fun searchUserById(userId: Int) {
        // A API C# para obter usuário por ID não requer token de autorização (por enquanto)
        // Se requerer, precisará do token do AuthViewModel
        userSearchUiState = UserSearchUiState.Loading
        apiService.obterUsuarioPorId(userId = userId).enqueue(object : Callback<UsuarioDto> {
            override fun onResponse(call: Call<UsuarioDto>, response: Response<UsuarioDto>) {
                if (response.isSuccessful && response.body() != null) {
                    userSearchUiState = UserSearchUiState.Success(response.body()!!)
                } else if (response.code() == 404) {
                    userSearchUiState = UserSearchUiState.NotFound
                }
                else {
                    userSearchUiState = UserSearchUiState.Error("Erro ao buscar usuário: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<UsuarioDto>, t: Throwable) {
                userSearchUiState = UserSearchUiState.Error("Falha na conexão: ${t.message}")
            }
        })
    }

    fun setUsuarioParaVisualizar(user: User) {
        this.usuarioProcurado = user
    }
    fun getUsuarioParaVisualizar(): User? {
        val userToReturn = usuarioProcurado
        return userToReturn
    }
    fun resetSalaListState() { salaListUiState = SalaUiState.Idle }
    fun resetActiveSalaDetailState() { activeSalaDetailUiState = SalaUiState.Idle }
    fun resetUsuarioSalaState() { usuarioSalaUiState = UsuarioSalaUiState.Idle }
    fun resetMensagemState() { mensagemUiState = MensagemUiState.Idle }
    fun resetUserSearchState() { userSearchUiState = UserSearchUiState.Idle }

    // Helper para extrair mensagem de erro
    private fun String.extractMessage(): String {
        return try {
            val jsonObject = org.json.JSONObject(this)
            jsonObject.optString("Message", this)
        } catch (e: org.json.JSONException) {
            this
        }
    }
}