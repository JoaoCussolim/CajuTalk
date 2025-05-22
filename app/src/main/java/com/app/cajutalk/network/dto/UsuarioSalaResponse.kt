package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class UsuarioSalaResponse(
    @SerializedName("id") // No C# é Id
    val id: Int,

    @SerializedName("usuarioId") // No C# é UsuarioId
    val usuarioId: Int,

    @SerializedName("salaId") // No C# é SalaId
    val salaId: Int,

    @SerializedName("isCriador") // No C# é IsCriador
    val isCriador: Boolean,

    @SerializedName("isBanido") // No C# é IsBanido
    val isBanido: Boolean
)