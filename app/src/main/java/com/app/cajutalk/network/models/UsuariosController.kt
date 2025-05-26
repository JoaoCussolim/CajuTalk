package com.app.cajutalk.network.models

data class UsuarioDto(
    val ID: Int,
    val NomeUsuario: String,
    val LoginUsuario: String,
    val FotoPerfilURL: String?,
    val Recado: String?
)

data class UsuarioUpdateDto(
    val NomeUsuario: String?,
    val LoginUsuario: String?,
    val SenhaUsuario: String?, // New password
    val NovaFotoPerfil: String?, // URL of the new profile picture
    val Recado: String?
)