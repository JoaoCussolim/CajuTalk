// Em: com.app.cajutalk.network.dto
package com.app.cajutalk.network.dto

import com.google.gson.annotations.SerializedName
// Não precisamos de IFormFile aqui, pois o Retrofit lida com isso de forma diferente (MultipartBody.Part)

// DTO para criar/enviar uma mensagem (corresponde a MensagemCreateDto.cs)
// Para uso com Retrofit @Multipart, os campos que não são arquivo serão RequestBody,
// então uma data class direta pode não ser usada para o corpo da requisição, mas é bom para referência.
data class MensagemCreateData( // Nome diferente para evitar confusão com o DTO de resposta
    val idSala: Int,
    val conteudo: String?, // Conteúdo textual, pode ser nulo se for só arquivo
    val tipoMensagem: String,
    // O arquivo será um MultipartBody.Part separado na chamada do Retrofit
)

// DTO para receber uma mensagem (corresponde a MensagemDto.cs)
data class MensagemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("salaId")
    val salaId: Int,
    @SerializedName("conteudo")
    val conteudo: String,
    @SerializedName("dataEnvio") // API envia DateTime, Gson converte para String
    val dataEnvio: String,
    @SerializedName("tipoMensagem")
    val tipoMensagem: String,
    @SerializedName("usuarioId")
    val usuarioId: Int,
    @SerializedName("loginUsuario")
    val loginUsuario: String,
    @SerializedName("fotoPerfilURL")
    val fotoPerfilURL: String?
)