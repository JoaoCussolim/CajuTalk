package com.app.cajutalk

data class User(
    val id: Int = 0, // ID vindo da API
    val login: String,
    var name: String,
    var imageUrl: String? = null,
    val senha: String = "", // Usado apenas para preenchimento de formulário, não para estado logado
    var message: String = ""// 'message' se não for usado ou adicione se necessário
)