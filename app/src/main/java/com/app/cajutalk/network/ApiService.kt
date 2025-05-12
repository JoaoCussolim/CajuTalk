// Em: com.app.cajutalk.network (ou seu pacote)
package com.app.cajutalk.network

import com.app.cajutalk.network.dto.LoginRequest
import com.app.cajutalk.network.dto.RegisterRequest
import com.app.cajutalk.network.dto.TokenResponse
import com.app.cajutalk.network.dto.RefreshTokenRequest
import com.app.cajutalk.network.dto.MensagemDto // << VERIFIQUE ESTE IMPORT
import com.app.cajutalk.network.dto.UsuarioDto  // << VERIFIQUE ESTE IMPORT
// MensagemCreateData não é usado diretamente como parâmetro de método aqui
// import com.app.cajutalk.network.dto.MensagemCreateData

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // --- Auth Endpoints ---
    @POST("auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<TokenResponse>

    @POST("auth/login")
    fun loginUser(@Body request: LoginRequest): Call<TokenResponse>

    @POST("auth/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<TokenResponse>

    // --- Message Endpoints ---
    @Multipart
    @POST("mensagens") // Rota base do seu MensagensController
    fun enviarMensagem(
        @Header("Authorization") token: String,
        @Part("IDSala") idSala: RequestBody,             // Nome do campo na API: IDSala
        @Part("Conteudo") conteudo: RequestBody?,        // Nome do campo na API: Conteudo
        @Part("TipoMensagem") tipoMensagem: RequestBody, // Nome do campo na API: TipoMensagem
        @Part arquivo: MultipartBody.Part?              // Nome do campo na API: Arquivo
    ): Call<MensagemDto> // Retorna MensagemDto

    @GET("mensagens/sala/{idSala}") // Rota base + /sala/{idSala}
    fun obterMensagensPorSala(
        @Header("Authorization") token: String,
        @Path("idSala") idSala: Int
    ): Call<List<MensagemDto>> // Retorna lista de MensagemDto

    // --- User Endpoint ---
    @GET("usuarios/{id}") // Rota base do UsuariosController + /{id}
    fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Call<UsuarioDto> // Retorna UsuarioDto

    // ... Outros endpoints ...
}