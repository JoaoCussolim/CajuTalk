package com.app.cajutalk.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        if (fileName == null) {
            fileName = "temp_file_${System.currentTimeMillis()}"
        }

        val tempFile = File(context.cacheDir, fileName)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
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

    // Add this new function inside the MensagemRepository class
    suspend fun uploadFileAndGetUrl(arquivoUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // A dummy room ID is required by the endpoint. Your backend might have a
                // specific room for this, or you can use any valid ID. Let's use 1 as an example.
                val idSalaRb = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                val tipoMensagemRb = "Imagem".toRequestBody("text/plain".toMediaTypeOrNull())

                val file = getFileFromUri(applicationContext, arquivoUri)
                if (file == null || !file.exists()) {
                    return@withContext Result.failure(Exception("Could not access file from Uri."))
                }

                val mimeType = applicationContext.contentResolver.getType(arquivoUri) ?: "application/octet-stream"
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val arquivoPart = MultipartBody.Part.createFormData("Arquivo", file.name, requestFile)

                // Call the existing endpoint for sending messages
                val response = apiService.enviarMensagem(
                    idSala = idSalaRb,
                    conteudo = null, // No text content needed
                    tipoMensagem = tipoMensagemRb,
                    arquivo = arquivoPart
                )

                if (response.isSuccessful && response.body() != null) {
                    // The API should return the public URL in the 'Conteudo' field
                    val fileUrl = response.body()!!.Conteudo
                    Result.success(fileUrl)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to upload file: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}