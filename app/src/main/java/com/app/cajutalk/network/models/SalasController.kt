package com.app.cajutalk.network.models

data class UsuarioDaSalaDto(
    val UsuarioId: Int,
    val LoginUsuario: String,
    val FotoPerfilURL: String?,
    val IsCriador: Boolean,
    val IsBanido: Boolean
)

data class SalaCreateDto(
    val Nome: String,
    val Publica: Boolean,
    val Senha: String?,
    val FotoPerfilURL: String?
)

data class SalaChatDto(
    val ID: Int,
    val Nome: String,
    val Publica: Boolean,
    val FotoPerfilURL: String?,
    val CriadorID: Int
)