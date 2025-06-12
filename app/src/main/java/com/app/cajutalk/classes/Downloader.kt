package com.app.cajutalk.classes

interface Downloader {
    fun downloadFile(url: String, fileName: String, mimeType: String): Long
}