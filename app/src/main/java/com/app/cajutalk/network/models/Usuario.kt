package com.app.cajutalk.network.models

data class Usuario(
    val ID: Int,
    val NomeUsuario: String,
    val LoginUsuario: String,
    val SenhaHash: String,
    val FotoPerfilURL: String?,
    val CorFundo: String?,
    val Recado: String?
)