package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class UsuarioUpdateRequest(
    @SerializedName("nomeUsuario")
    val nomeUsuario: String?, // Nullable para atualizações parciais

    @SerializedName("loginUsuario")
    val loginUsuario: String?, // Nullable

    @SerializedName("senhaUsuario")
    val senhaUsuario: String?, // Nullable, nova senha em texto plano

    @SerializedName("novaFotoPerfil") // Na API C# é NovaFotoPerfil, uma string (URL ou Base64)
    val novaFotoPerfil: String?,
)