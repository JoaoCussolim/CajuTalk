package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class EntrarSalaRequest(
    @SerializedName("salaId") // No C# é SalaId
    val salaId: Int,

    @SerializedName("senha")
    val senha: String? // Nullable
)