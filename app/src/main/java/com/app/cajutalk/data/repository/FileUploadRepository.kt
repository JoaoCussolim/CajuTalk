package com.app.cajutalk.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.app.cajutalk.network.ApiService
import com.app.cajutalk.network.models.UploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class FileUploadRepository(
    private val apiService: ApiService,
    private val applicationContext: Context
) {

    suspend fun uploadFile(fileUri: Uri): Result<UploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val file = getFileFromUri(applicationContext, fileUri)
                    ?: return@withContext Result.failure(Exception("Não foi possível acessar o arquivo a partir do Uri."))

                // Tenta obter o tipo MIME do arquivo, usa um genérico como fallback.
                val mimeType = applicationContext.contentResolver.getType(fileUri) ?: "application/octet-stream"
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())

                // O nome do campo "file" deve corresponder ao esperado pelo controller ASP.NET ([FromForm] IFormFile file)
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Chama a função genérica da ApiService
                val response = apiService.uploadFile(filePart)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha no upload do arquivo: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteFile(fileName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            // Verifica se o nome do arquivo não é uma URL completa
            val sanitizedFileName = fileName.substringAfterLast('/')

            if (sanitizedFileName.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Nome de arquivo inválido."))
            }

            try {
                // Chama a nova função deleteFile da ApiService
                val response = apiService.deleteFile(sanitizedFileName)

                if (response.isSuccessful) {
                    Result.success(Unit) // Sucesso, não há corpo para retornar
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao deletar o arquivo: ${response.code()}"
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
        // Se não conseguir, gera um nome temporário genérico
        if (fileName == null) {
            fileName = "temp_file_${System.currentTimeMillis()}"
        }

        val tempFile = File(context.cacheDir, fileName)
        try {
            // Copia o conteúdo do Uri para o arquivo temporário
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
}