package com.app.cajutalk.network.models

data class EntrarSalaDto(
    val SalaId: Int,
    val Senha: String?
)

data class UsuarioSalaDto(
    val Id: Int,
    val UsuarioId: Int,
    val SalaId: Int,
    val IsCriador: Boolean,
    val IsBanido: Boolean
)