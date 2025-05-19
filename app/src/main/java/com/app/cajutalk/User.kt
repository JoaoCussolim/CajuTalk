package com.app.cajutalk

import com.app.cajutalk.network.dto.UsuarioDto

data class User(
    val id: Int = 0, // ID vindo da API
    val login: String,
    var name: String,
    var imageUrl: String? = null,
    val senha: String = "", // Usado apenas para preenchimento de formulário, não para estado logado
    var message: String = ""// 'message' se não for usado ou adicione se necessário
){
    constructor(dto: UsuarioDto) : this(
        id = dto.id,
        login = dto.loginUsuario,
        senha = "", // Senha não vem da API neste DTO
        name = dto.nomeUsuario,
        imageUrl = dto.fotoPerfilURL
    )
}