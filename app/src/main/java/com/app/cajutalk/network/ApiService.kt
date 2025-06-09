package com.app.cajutalk.network

import com.app.cajutalk.network.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call // Para a chamada síncrona no Authenticator
import retrofit2.Response // Use Response<T> para ter acesso a headers e código de status
import retrofit2.http.*

interface ApiService {

    // --- AuthController ---
    @POST("auth/login")
    suspend fun login(@Body loginModel: LoginModel): Response<TokenResponse>

    @POST("auth/register")
    suspend fun register(@Body registerModel: RegisterModel): Response<TokenResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<TokenResponse>

    // Versão SÍNCRONA para o Authenticator (não é suspend)
    @POST("auth/refresh")
    fun refreshTokenSync(@Body refreshTokenRequest: RefreshTokenRequest): Call<TokenResponse>


    // --- UsuariosController ---
    @GET("usuarios")
    suspend fun obterTodosUsuarios(): Response<List<UsuarioDto>>

    @GET("usuarios/{id}")
    suspend fun obterUsuarioPorId(@Path("id") id: Int): Response<UsuarioDto>

    @GET("usuarios/buscar")
    suspend fun buscarUsuarios(@Query("termoBusca") termoBusca: String): Response<List<UsuarioDto>>

    @PUT("usuarios/{id}")
    suspend fun atualizarUsuario(
        @Path("id") id: Int,
        @Body usuarioUpdateDto: UsuarioUpdateDto
    ): Response<Usuario> // API retorna Usuario completo

    @DELETE("usuarios/{id}")
    suspend fun deletarUsuario(@Path("id") id: Int): Response<Unit> // Para 204 No Content


    // --- SalasController ---
    @GET("salas/{salaId}/usuarios")
    suspend fun getUsuariosDaSala(@Path("salaId") salaId: Int): Response<List<UsuarioDaSalaDto>>

    @DELETE("salas/{id}")
    suspend fun deletarSala(@Path("id") id: Int): Response<Unit>

    @POST("salas")
    suspend fun criarSala(@Body salaCreateDto: SalaCreateDto): Response<SalaChatDto> // Espera 201 Created

    @GET("salas")
    suspend fun obterTodasSalas(): Response<List<SalaChatDto>>

    @GET("salas/{id}")
    suspend fun obterSalaPorId(@Path("id") id: Int): Response<SalaChatDto>


    // --- UsuarioSalaController ---
    @POST("usuariosala/entrar")
    suspend fun entrarSala(@Body entrarSalaDto: EntrarSalaDto): Response<UsuarioSalaDto>

    @DELETE("usuariosala/sair/{salaId}")
    suspend fun sairSala(@Path("salaId") salaId: Int): Response<Unit>

    @PUT("usuariosala/{salaId}/usuarios/{usuarioIdParaBanir}/banir")
    suspend fun banirUsuario(
        @Path("salaId") salaId: Int,
        @Path("usuarioIdParaBanir") usuarioIdParaBanir: Int
    ): Response<UsuarioSalaDto>

    @PUT("usuariosala/{salaId}/usuarios/{usuarioIdParaDesbanir}/desbanir")
    suspend fun desbanirUsuario(
        @Path("salaId") salaId: Int,
        @Path("usuarioIdParaDesbanir") usuarioIdParaDesbanir: Int
    ): Response<UsuarioSalaDto>

    @GET("usuariosala/salas/{salaId}/usuarios/{usuarioId}")
    suspend fun getRelacaoUsuarioSala(
        @Path("salaId") salaId: Int,
        @Path("usuarioId") usuarioId: Int
    ): Response<UsuarioSalaDto>


    // --- MensagensController ---
    @Multipart // Indica que é uma requisição multipart
    @POST("mensagens")
    suspend fun enviarMensagem(
        @Part("IDSala") idSala: RequestBody, // Use RequestBody para campos não-arquivo
        @Part("Conteudo") conteudo: RequestBody?,
        @Part("TipoMensagem") tipoMensagem: RequestBody?,
        @Part arquivo: MultipartBody.Part? // Para o arquivo em si
    ): Response<MensagemDto>

    @GET("mensagens/sala/{idSala}")
    suspend fun obterMensagensPorSala(@Path("idSala") idSala: Int): Response<List<MensagemDto>>

    @Multipart
    @POST("upload/file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @DELETE("upload/{fileName}")
    suspend fun deleteFile(
        @Path("fileName") fileName: String
    ): Response<Void>
}