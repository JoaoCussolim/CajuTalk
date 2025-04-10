package com.app.cajutalk

import java.io.Serializable

data class Sala(
    val nome: String,
    val membros: List<User>,
    val senha: String,
    val imageUrl: String,
    val mensagens: List<Mensagem>,
    val criador: User
) : Serializable {

    fun getMembrosToString(): String {
        return membros.joinToString(", ") { it.name }
    }
}
