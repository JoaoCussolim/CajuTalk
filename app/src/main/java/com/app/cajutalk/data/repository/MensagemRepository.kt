package com.app.cajutalk.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.models.MensagemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class MensagemRepository(
    private val apiService: ApiService,
    private val applicationContext: Context
) {
    suspend fun enviarMensagem(
        idSala: Int,
        conteudoTexto: String?,
        tipoMensagem: String?,
        arquivoUri: Uri?
    ): Result<MensagemDto> {
        return withContext(Dispatchers.IO) {
            try {
                val idSalaRb = idSala.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val conteudoRb: RequestBody? = conteudoTexto?.toRequestBody("text/plain".toMediaTypeOrNull())
                val tipoMensagemRb: RequestBody? = tipoMensagem?.toRequestBody("text/plain".toMediaTypeOrNull())

                var arquivoPart: MultipartBody.Part? = null
                if (arquivoUri != null) {
                    val file = getFileFromUri(applicationContext, arquivoUri)
                    if (file != null && file.exists()) {
                        val mimeType = applicationContext.contentResolver.getType(arquivoUri) ?: "application/octet-stream"
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        arquivoPart = MultipartBody.Part.createFormData("Arquivo", file.name, requestFile)
                    } else {
                        return@withContext Result.failure(Exception("Não foi possível acessar o arquivo a partir do Uri."))
                    }
                }

                if (conteudoTexto.isNullOrBlank() && arquivoPart == null) {
                    return@withContext Result.failure(IllegalArgumentException("A mensagem deve conter texto ou um arquivo."))
                }

                val response = apiService.enviarMensagem(
                    idSala = idSalaRb,
                    conteudo = conteudoRb,
                    tipoMensagem = tipoMensagemRb,
                    arquivo = arquivoPart
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao enviar mensagem: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMensagensPorSala(salaId: Int): Result<List<MensagemDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obterMensagensPorSala(salaId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao buscar mensagens da sala: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        var fileName: String? = null

        // Tenta obter o nome do arquivo do ContentResolver (funciona para Uris de 'content://')
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignora exceções, tentaremos outros métodos
        }

        if (fileName == null) {
            fileName = uri.path?.let { File(it).name }
        }

        if (fileName == null) {
            val mimeType = contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            fileName = "temp_file_${System.currentTimeMillis()}" + (extension?.let { ".$it" } ?: "")
        }

        val tempFile = File(context.cacheDir, fileName!!)
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}