package com.app.cajutalk

import androidx.compose.ui.graphics.Color
import java.io.Serializable

data class Sala(
    val nome: String,
    val membros: String,
    val senha: String,
    val imageUrl: String,
    val mensagens: List<Mensagem>,
    val criador: User
) : Serializable