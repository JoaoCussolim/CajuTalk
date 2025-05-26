package com.app.cajutalk.network.models

data class LoginModel(
    val LoginUsuario: String,
    val SenhaUsuario: String
)

data class TokenResponse(
    val AccessToken: String,
    val RefreshToken: String,
    val AccessTokenExpiration: String // ISO 8601 DateTime string
)

data class RefreshTokenRequest(
    val RefreshToken: String
)

data class RegisterModel(
    val NomeUsuario: String,
    val LoginUsuario: String,
    val SenhaUsuario: String
)