package com.app.cajutalk.network.models

import com.google.gson.annotations.SerializedName

data class UsuarioDto(
    @SerializedName("id")
    val ID: Int,

    @SerializedName("nomeUsuario")
    val NomeUsuario: String?,

    @SerializedName("loginUsuario")
    val LoginUsuario: String?,

    @SerializedName("fotoPerfilURL")
    val FotoPerfilURL: String?,

    @SerializedName("recado")
    val Recado: String?
)

data class UsuarioUpdateDto(
    @SerializedName("nomeUsuario")
    val NomeUsuario: String?,

    @SerializedName("loginUsuario")
    val LoginUsuario: String?,

    @SerializedName("senhaUsuario")
    val SenhaUsuario: String?, // New password

    @SerializedName("novaFotoPerfil")
    val NovaFotoPerfil: String?, // URL of the new profile picture

    @SerializedName("recado")
    val Recado: String?
)