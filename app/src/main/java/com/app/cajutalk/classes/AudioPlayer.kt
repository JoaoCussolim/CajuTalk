package com.app.cajutalk.classes

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: IllegalStateException) {
            false
        }
    }

    fun playAudio(context: Context, audioUrl: String, onCompletion: () -> Unit) {
        stopAudio() // Para e libera qualquer player anterior

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(audioUrl))

                setOnPreparedListener {
                    Log.d("AudioPlayer", "Áudio pronto! Iniciando reprodução.")
                    start()
                }

                setOnCompletionListener {
                    Log.d("AudioPlayer", "Reprodução concluída.")
                    onCompletion()
                    stopAudio() // Libera os recursos ao terminar
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Erro no MediaPlayer: what=$what, extra=$extra")
                    stopAudio()
                    true
                }

                prepareAsync() // Prepara o áudio de forma assíncrona
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Erro ao preparar áudio: ${e.message}")
        }
    }

    fun pauseAudio() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun resumeAudio() {
        mediaPlayer?.start()
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: IllegalStateException) {
            0
        }
    }

    fun stopAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}