package com.app.cajutalk.network.models

import com.google.gson.annotations.SerializedName

data class LoginModel(
    val LoginUsuario: String,
    val SenhaUsuario: String
)

data class TokenResponse(
    @SerializedName("accessToken")
    val AccessToken: String,
    @SerializedName("refreshToken")
    val RefreshToken: String,
    @SerializedName("accessTokenExpiration")
    val AccessTokenExpiration: String
)


data class RefreshTokenRequest(
    val RefreshToken: String
)

data class RegisterModel(
    val NomeUsuario: String,
    val LoginUsuario: String,
    val SenhaUsuario: String
)