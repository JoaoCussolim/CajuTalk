package com.app.cajutalk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class EstadoSala(
    var sala: Sala?,
    var membros: List<User>
)

class DataViewModel : ViewModel() {
    var estadoSala by mutableStateOf(EstadoSala(null, emptyList()))
    var usuarioProcurado by mutableStateOf<User?>(null)
}