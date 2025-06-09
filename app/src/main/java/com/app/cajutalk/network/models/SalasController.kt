package com.app.cajutalk.network.models

import com.google.gson.annotations.SerializedName

data class UsuarioDaSalaDto(
    @SerializedName("usuarioId")
    val UsuarioId: Int,

    @SerializedName("loginUsuario")
    val LoginUsuario: String,

    @SerializedName("fotoPerfilURL")
    val FotoPerfilURL: String?,

    @SerializedName("isCriador")
    val IsCriador: Boolean,

    @SerializedName("isBanido")
    val IsBanido: Boolean
)

data class SalaCreateDto(
    @SerializedName("nome")
    val Nome: String,

    @SerializedName("publica")
    val Publica: Boolean,

    @SerializedName("senha")
    val Senha: String?,

    @SerializedName("fotoPerfilURL")
    val FotoPerfilURL: String?
)

data class SalaChatDto(
    @SerializedName("id")
    val ID: Int,

    @SerializedName("nome")
    val Nome: String,

    @SerializedName("publica")
    val Publica: Boolean,

    @SerializedName("fotoPerfilURL")
    val FotoPerfilURL: String?,

    @SerializedName("criadorID")
    val CriadorID: Int
)