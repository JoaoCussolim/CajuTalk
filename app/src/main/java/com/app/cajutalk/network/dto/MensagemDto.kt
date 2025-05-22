package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName

data class MensagemDto(
    @SerializedName("id") // No C# é Id
    val id: Int,

    @SerializedName("salaId") // No C# é SalaId
    val salaId: Int,

    @SerializedName("conteudo")
    val conteudo: String, // Pode ser texto ou URL do arquivo

    @SerializedName("dataEnvio") // A API retorna DateTime, Gson geralmente converte para String
    val dataEnvio: String, // Ou java.util.Date se você configurar o Gson

    @SerializedName("tipoMensagem")
    val tipoMensagem: String, // "Texto", "Imagem", "Video", "Audio", "Arquivo"

    @SerializedName("usuarioId") // No C# é UsuarioId
    val usuarioId: Int,

    @SerializedName("loginUsuario")
    val loginUsuario: String,

    @SerializedName("fotoPerfilURL")
    val fotoPerfilURL: String?
)