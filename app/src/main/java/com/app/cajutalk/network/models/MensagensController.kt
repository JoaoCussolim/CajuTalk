package com.app.cajutalk.network.models

import com.google.gson.annotations.SerializedName

data class MensagemDto(
    @SerializedName("id"    )
    val Id: Int,

    @SerializedName("salaID")
    val SalaId: Int,

    @SerializedName("conteudo")
    val Conteudo: String, // Text content OR URL to the uploaded file

    @SerializedName("dataEnvio")
    val DataEnvio: String, // ISO 8601 DateTime string

    @SerializedName("tipoMensagem")
    val TipoMensagem: String,

    @SerializedName("usuarioID")
    val UsuarioId: Int,

    @SerializedName("loginUsuario")
    val LoginUsuario: String,

    @SerializedName("fotoPerfilURL")
    val FotoPerfilURL: String?
)