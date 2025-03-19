package com.app.cajutalk

import androidx.compose.ui.graphics.Color

data class Sala(val nome: String, val membros: String, val senha: String, val imageUrl: String, val mensagens: List<Mensagem>)