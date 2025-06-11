package com.app.cajutalk.network.models

import com.google.gson.annotations.SerializedName

data class EntrarSalaDto(
    val SalaId: Int,
    val Senha: String?
)

data class UsuarioSalaDto(
    @SerializedName("id")
    val Id: Int,

    @SerializedName("usuarioID")
    val UsuarioId: Int,

    @SerializedName("salaID")
    val SalaId: Int,

    @SerializedName("isCriador")
    val IsCriador: Boolean,

    @SerializedName("isBanido")
    val IsBanido: Boolean
)