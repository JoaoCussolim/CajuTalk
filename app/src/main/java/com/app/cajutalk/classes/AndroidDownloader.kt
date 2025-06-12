package com.app.cajutalk.classes

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

class AndroidDownloader(
    private val context: Context
): Downloader {

    // Acessa o serviço de Download do Android
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String, fileName: String, mimeType: String): Long {
        // Cria uma requisição de download
        val request = DownloadManager.Request(url.toUri())
            .setMimeType(mimeType)
            // Permite o download tanto em Wi-Fi quanto em dados móveis
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName) // Título da notificação
            .setDescription("Baixando arquivo...") // Descrição da notificação
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        return downloadManager.enqueue(request)
    }
}