package com.app.cajutalk.viewmodels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import java.io.File

class AudioRecorderViewModel : ViewModel() {
    private var mediaRecorder: MediaRecorder? = null
    var audioPath by mutableStateOf<String?>(null)
        private set

    fun hasPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: Activity) {
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(activity, permissions, 100)
    }

    fun startRecording(context: Context) {
        if (!hasPermissions(context)) {
            Log.e("AudioRecorder", "Permissão negada! Solicite permissão antes de gravar.")
            return
        }

        try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            if (dir == null || (!dir.exists() && !dir.mkdirs())) {
                throw Exception("Erro ao acessar o diretório de áudio")
            }

            // O nome do arquivo com extensão .mp4 está correto para o formato que vamos usar
            val file = File(dir, "audio_${System.currentTimeMillis()}.mp4")
            audioPath = file.absolutePath

            // MUDANÇA: Usando API Level-aware para MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                // MUDANÇA: Formato de saída para MP4
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                // MUDANÇA: Codec de áudio para AAC (padrão para MP4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioPath)

                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AudioRecorder", "Erro ao iniciar a gravação: ${e.message}")
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AudioRecorder", "Erro ao parar a gravação: ${e.message}")
        }
    }
}