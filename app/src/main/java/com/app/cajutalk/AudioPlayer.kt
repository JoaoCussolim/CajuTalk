package com.app.cajutalk

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(context: Context, audioPath: String, onCompletion: () -> Unit) {
        stopAudio()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.parse(audioPath))
            prepare()
            start()
            setOnCompletionListener {
                onCompletion()
            }
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

    private fun stopAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
