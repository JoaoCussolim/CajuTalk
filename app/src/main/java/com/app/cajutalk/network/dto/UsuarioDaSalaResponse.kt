package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class UsuarioDaSalaResponse(
    @SerializedName("usuarioId")
    val usuarioId: Int,

    @SerializedName("loginUsuario")
    val loginUsuario: String,

    @SerializedName("fotoPerfilURL") // Assegure que a API envia este campo consistentemente
    val fotoPerfilURL: String?,

    @SerializedName("isCriador")
    val isCriador: Boolean,

    @SerializedName("isBanido")
    val isBanido: Boolean
)