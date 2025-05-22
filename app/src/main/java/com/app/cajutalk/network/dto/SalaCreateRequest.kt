package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class SalaCreateRequest(
    @SerializedName("nome")
    val nome: String,

    @SerializedName("publica")
    val publica: Boolean,

    @SerializedName("senha")
    val senha: String?, // Nullable

    @SerializedName("fotoPerfilURL")
    val fotoPerfilURL: String? // Nullable
)