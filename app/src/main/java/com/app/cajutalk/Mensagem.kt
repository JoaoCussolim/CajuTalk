package com.app.cajutalk

import android.net.Uri
import java.time.LocalDateTime

data class Mensagem(val idApi : Int,
                    val texto: String,
                    val nomeArquivo: String,
                    val uriArquivo: Uri?,
                    val isUser: Boolean,
                    val data: LocalDateTime,
                    val urlDaApi : String,
                    val tipoApi : String,
                    val senderLoginApi : String,
                    val senderNameApi : String,
                    val senderImageUrlApi : String)