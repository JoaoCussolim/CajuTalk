// Em: com.app.cajutalk.network.dto
package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class UsuarioDto(
    @SerializedName("id") // Corresponde a UsuarioDto.ID na API
    val id: Int,
    @SerializedName("nomeUsuario") // Corresponde a UsuarioDto.NomeUsuario na API
    val nomeUsuario: String,
    @SerializedName("loginUsuario") // Corresponde a UsuarioDto.LoginUsuario na API
    val loginUsuario: String,
    @SerializedName("fotoPerfilURL") // Corresponde a UsuarioDto.FotoPerfilURL na API
    val fotoPerfilURL: String?,
    @SerializedName("corFundo") // Corresponde a UsuarioDto.CorFundo na API
    val corFundo: String?
)