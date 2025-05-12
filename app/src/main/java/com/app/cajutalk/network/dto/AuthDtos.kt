// Em: com.app.cajutalk.network.dto (ou seu pacote preferido para DTOs)

package com.app.cajutalk.network.dto // Adapte o pacote conforme sua estrutura

import com.google.gson.annotations.SerializedName

// Para o request de AuthController.Register
data class RegisterRequest(
    @SerializedName("nomeUsuario") // Corresponde a RegisterModel.NomeUsuario na API
    val nomeUsuario: String,
    @SerializedName("loginUsuario") // Corresponde a RegisterModel.LoginUsuario na API
    val loginUsuario: String,
    @SerializedName("senhaUsuario") // Corresponde a RegisterModel.SenhaUsuario na API
    val senhaUsuario: String
)

// Para o request de AuthController.Login
data class LoginRequest(
    @SerializedName("loginUsuario") // Corresponde a LoginModel.LoginUsuario na API
    val loginUsuario: String,
    @SerializedName("senhaUsuario") // Corresponde a LoginModel.SenhaUsuario na API
    val senhaUsuario: String
)

// Para o response de AuthController.Login e AuthController.Register (ambos usam TokenResponse)
data class TokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("accessTokenExpiration") // A API retorna DateTime, Gson geralmente converte para String
    val accessTokenExpiration: String // Ou java.util.Date se você configurar o Gson
)

// Para o request de AuthController.Refresh
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

// DTO para representar os dados do usuário que podem vir em algumas respostas
// ou que você pode querer usar internamente.
// Sua API parece retornar o TokenResponse diretamente sem um UserDto aninhado nas respostas de login/registro.
// Se a API for atualizada para incluir UserDto na resposta do login/registro (como no exemplo anterior),
// você adicionaria aqui:
// data class UserDto(
//    @SerializedName("id") val id: Int,
//    @SerializedName("nomeUsuario") val nomeUsuario: String,
//    @SerializedName("loginUsuario") val loginUsuario: String,
//    @SerializedName("fotoPerfilURL") val fotoPerfilURL: String?
// )
// e TokenResponse seria:
// data class TokenResponse(
//    /* ... tokens ... */
//    @SerializedName("usuario") val usuario: UserDto // Se a API enviar
// )