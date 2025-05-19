package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class SalaChatResponse(
    @SerializedName("id") // No C# é ID
    val id: Int,

    @SerializedName("nome")
    val nome: String,

    @SerializedName("publica")
    val publica: Boolean,

    @SerializedName("fotoPerfilURL")
    val fotoPerfilURL: String?,

    @SerializedName("criadorID") // No C# é CriadorID
    val criadorID: Int
)