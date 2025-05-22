package com.app.cajutalk.classes

import android.net.Uri
import java.time.LocalDateTime

data class Mensagem(val texto: String, val nomeArquivo: String, val uriArquivo: Uri?, val isUser: Boolean, val data: LocalDateTime)