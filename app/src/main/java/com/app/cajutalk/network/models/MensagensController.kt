package com.app.cajutalk.network.models

data class MensagemDto(
    val Id: Int,
    val SalaId: Int,
    val Conteudo: String, // Text content OR URL to the uploaded file
    val DataEnvio: String, // ISO 8601 DateTime string
    val TipoMensagem: String,
    val UsuarioId: Int,
    val LoginUsuario: String,
    val FotoPerfilURL: String?
)