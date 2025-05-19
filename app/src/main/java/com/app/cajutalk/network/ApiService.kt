package com.app.cajutalk.network

import com.app.cajutalk.network.dto.LoginRequest
import com.app.cajutalk.network.dto.RegisterRequest
import com.app.cajutalk.network.dto.TokenResponse
import com.app.cajutalk.network.dto.RefreshTokenRequest
import com.app.cajutalk.network.dto.UsuarioDto
import com.app.cajutalk.network.dto.UsuarioUpdateRequest // Assuming you create this
import com.app.cajutalk.network.dto.SalaCreateRequest    // Assuming you create this
import com.app.cajutalk.network.dto.SalaChatResponse    // Assuming you create this
import com.app.cajutalk.network.dto.UsuarioDaSalaResponse // Assuming you create this
import com.app.cajutalk.network.dto.MensagemDto
// MensagemCreateRequest is handled by @Part, not a separate DTO class for @Body
import com.app.cajutalk.network.dto.EntrarSalaRequest   // Assuming you create this
import com.app.cajutalk.network.dto.UsuarioSalaResponse // Assuming you create this

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // --- AuthController ---

    @POST("auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<TokenResponse>

    @POST("auth/login")
    fun loginUser(@Body request: LoginRequest): Call<TokenResponse>

    @POST("auth/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<TokenResponse>

    // --- UsuariosController ---

    @GET("usuarios")
    fun obterTodosUsuarios( // Corresponds to UsuariosController.ObterTodos
        // C# API is public, but usually listing users might require auth.
        // If it becomes protected, add: @Header("Authorization") token: String
    ): Call<List<UsuarioDto>>

    @GET("usuarios/{id}")
    fun obterUsuarioPorId( // Corresponds to UsuariosController.ObterPorId
        @Path("id") userId: Int
        // C# API is public. If it becomes protected, add: @Header("Authorization") token: String
        // Your example getUserById had a token, so keeping it here for consistency if you make it protected
        // @Header("Authorization") token: String,
    ): Call<UsuarioDto>

    @PUT("usuarios/{id}")
    fun atualizarUsuario( // Corresponds to UsuariosController.AtualizarUsuario
        @Header("Authorization") token: String, // Assuming this becomes protected
        @Path("id") userId: Int,
        @Body request: UsuarioUpdateRequest
    ): Call<UsuarioDto> // API returns Usuario model, ideal would be UsuarioDto. Mapping to UsuarioDto here.

    @DELETE("usuarios/{id}")
    fun deletarUsuario( // Corresponds to UsuariosController.DeletarUsuario
        @Header("Authorization") token: String, // Assuming this becomes protected
        @Path("id") userId: Int
    ): Call<Void> // For 204 No Content

    // --- SalasController ---

    @GET("salas/{salaId}/usuarios")
    fun getUsuariosDaSala( // Corresponds to SalasController.GetUsuariosDaSala
        @Header("Authorization") token: String,
        @Path("salaId") salaId: Int
    ): Call<List<UsuarioDaSalaResponse>>

    @DELETE("salas/{id}")
    fun deletarSala( // Corresponds to SalasController.DeletarSala
        @Header("Authorization") token: String,
        @Path("id") salaId: Int
    ): Call<Void>

    @POST("salas")
    fun criarSala( // Corresponds to SalasController.CriarSala
        @Header("Authorization") token: String,
        @Body request: SalaCreateRequest
    ): Call<SalaChatResponse>

    @GET("salas")
    fun obterTodasSalas( // Corresponds to SalasController.ObterTodas
        // This is AllowAnonymous in C#
    ): Call<List<SalaChatResponse>>

    @GET("salas/{id}")
    fun obterSalaPorId( // Corresponds to SalasController.ObterPorId
        // This is AllowAnonymous in C#
        @Path("id") salaId: Int
    ): Call<SalaChatResponse>

    // --- MensagensController ---

    @Multipart
    @POST("mensagens")
    fun enviarMensagem( // Corresponds to MensagensController.EnviarMensagem
        @Header("Authorization") token: String,
        @Part("IDSala") idSala: RequestBody,
        @Part("Conteudo") conteudo: RequestBody, // API expects string, even for file URL
        @Part("TipoMensagem") tipoMensagem: RequestBody,
        @Part arquivo: MultipartBody.Part?
    ): Call<MensagemDto>

    @GET("mensagens/sala/{idSala}")
    fun obterMensagensPorSala( // Corresponds to MensagensController.ObterMensagensPorSala
        @Header("Authorization") token: String,
        @Path("idSala") idSala: Int
    ): Call<List<MensagemDto>>

    // --- UsuarioSalaController ---

    @POST("usuariosala/entrar")
    fun entrarSala( // Corresponds to UsuarioSalaController.EntrarSala
        @Header("Authorization") token: String,
        @Body request: EntrarSalaRequest
    ): Call<UsuarioSalaResponse>

    @DELETE("usuariosala/sair/{salaId}")
    fun sairSala( // Corresponds to UsuarioSalaController.SairSala
        @Header("Authorization") token: String,
        @Path("salaId") salaId: Int
    ): Call<Void>

    @PUT("usuariosala/{salaId}/usuarios/{usuarioIdParaBanir}/banir")
    fun banirUsuario( // Corresponds to UsuarioSalaController.BanirUsuario
        @Header("Authorization") token: String,
        @Path("salaId") salaId: Int,
        @Path("usuarioIdParaBanir") usuarioIdParaBanir: Int
    ): Call<UsuarioSalaResponse>

    @PUT("usuariosala/{salaId}/usuarios/{usuarioIdParaDesbanir}/desbanir")
    fun desbanirUsuario( // Corresponds to UsuarioSalaController.DesbanirUsuario
        @Header("Authorization") token: String,
        @Path("salaId") salaId: Int,
        @Path("usuarioIdParaDesbanir") usuarioIdParaDesbanir: Int
    ): Call<UsuarioSalaResponse>

    @GET("usuariosala/salas/{salaId}/usuarios/{usuarioId}")
    fun getRelacaoUsuarioSala( // Corresponds to UsuarioSalaController.GetRelacaoUsuarioSala
        @Header("Authorization") token: String,
        @Path("salaId") salaId: Int,
        @Path("usuarioId") usuarioId: Int
    ): Call<UsuarioSalaResponse>


    // --- Your existing User Endpoint (from prompt) ---
    // This seems to overlap with obterUsuarioPorId but with an explicit token.
    // I'll keep it if you have a specific reason for it.
    // If `obterUsuarioPorId` is intended to be public (as per C#), this one might be for
    // fetching the current logged-in user's details IF the ID is known and you want to enforce auth.
    // However, typically, you'd have a dedicated "/me" endpoint for the current user.
    // For now, I've made `obterUsuarioPorId` public to match the C# [AllowAnonymous] (if it had one, or lack of [Authorize]).
    // If you make `obterUsuarioPorId` require auth, then this one is redundant.
    /*
    @GET("usuarios/{id}")
    fun getUserById( // This was in your provided ApiService
        @Header("Authorization") token: String, // This makes it different from a public obterUsuarioPorId
        @Path("id") userId: Int
    ): Call<UsuarioDto>
    */
    // Note: I've commented out your `getUserById` because `obterUsuarioPorId` serves the same C# endpoint.
    // If UsuariosController.ObterPorId becomes protected in C#, then `obterUsuarioPorId` should have the @Header("Authorization") token: String.
    // The example `getUserById` you provided earlier for `/usuarios/{id}` has an Authorization header.
    // I've adjusted `obterUsuarioPorId` to NOT require a token by default to match the C# API.
    // If your `UsuariosController.ObterPorId` action in C# is actually protected by [Authorize], then uncomment
    // the token header in `obterUsuarioPorId` and this `getUserById` becomes redundant.
    // For now, I've kept `obterUsuarioPorId` public and `obterTodosUsuarios` public as per the C# code not having [Authorize] on those specific actions.
    // You might want to review the C# UsuariosController and add [Authorize] to Get, Get(id), Put, Delete if they are meant to be protected.
}