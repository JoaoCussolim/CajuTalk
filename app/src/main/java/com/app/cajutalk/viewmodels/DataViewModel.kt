package com.app.cajutalk.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.app.cajutalk.network.models.SalaChatDto
import com.app.cajutalk.network.models.UsuarioDaSalaDto
import com.app.cajutalk.network.models.UsuarioDto

data class EstadoSala(
    var sala: SalaChatDto?,
    var membros: List<UsuarioDaSalaDto>
)

class DataViewModel : ViewModel() {
    var estadoSala by mutableStateOf(EstadoSala(null, emptyList()))
    var usuarioProcurado by mutableStateOf<UsuarioDto?>(null)
    var usuarioLogado by mutableStateOf<UsuarioDto?>(null)
}