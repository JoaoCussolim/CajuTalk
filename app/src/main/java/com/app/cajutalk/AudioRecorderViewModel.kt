package com.app.cajutalk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
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

            val file = File(dir, "audio_${System.currentTimeMillis()}.3gp")
            audioPath = file.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
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
