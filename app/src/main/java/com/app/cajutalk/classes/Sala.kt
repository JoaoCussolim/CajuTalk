package com.app.cajutalk.classes

import java.io.Serializable

data class Sala(
    val nome: String,
    val membros: List<User>,
    val senha: String,
    val imageUrl: String,
    val mensagens: List<Mensagem>,
    val criador: User,
    var privado: Boolean?,
) : Serializable {

    fun getMembrosToString(): String {
        return membros.joinToString(", ") { it.name }
    }
}
