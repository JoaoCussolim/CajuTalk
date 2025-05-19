package com.app.cajutalk

import android.net.Uri
import java.time.LocalDateTime

data class Mensagem(
    val idApi: Int? = null,
    var texto: String,
    var nomeArquivo: String?,
    var uriArquivo: Uri?,
    val isUser: Boolean,
    val data: LocalDateTime, // Ou o tipo que você preferir
    val urlDaApi: String? = null, // URL COMPLETA do arquivo na API (para download)
    val tipoApi: String? = null,  // "Texto", "Imagem", "Audio", etc. vindo da API
    val senderLoginApi: String? = null,
    var senderNameApi: String? = null, // Pode ser preenchido depois
    var senderImageUrlApi: String? = null,
    var isDownloading: Boolean = false, // Para UI de download
    var downloadProgress: Float = 0f   // Para UI de progresso
)
