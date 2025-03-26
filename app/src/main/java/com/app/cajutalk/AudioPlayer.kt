package com.app.cajutalk

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(context: Context, audioPath: String, onCompletion: () -> Unit) {
        stopAudio()

        val file = File(audioPath)
        if (!file.exists()) {
            println("Arquivo não encontrado: $audioPath")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(audioPath))
                prepareAsync()

                setOnPreparedListener {
                    println("Áudio pronto! Iniciando reprodução.")
                    start()
                }

                setOnCompletionListener {
                    println("Reprodução concluída")
                    onCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    println("Erro no MediaPlayer: what=$what, extra=$extra")
                    true
                }
            }
        } catch (e: Exception) {
            println("Erro ao preparar áudio: ${e.message}")
        }
    }


    fun pauseAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun setProgressListener(onProgress: (Int) -> Unit) {
        mediaPlayer?.apply {
            setOnSeekCompleteListener {
                onProgress(currentPosition)
            }
        }
    }

    private fun stopAudio() {
        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mediaPlayer = null
    }
}
