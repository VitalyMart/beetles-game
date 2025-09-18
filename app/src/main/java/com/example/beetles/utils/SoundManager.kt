package com.example.beetles.utils

import android.content.Context
import android.media.ToneGenerator
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.beetles.R

class SoundManager(private val context: Context) {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 50)
    private val beetleScreamPlayers = mutableListOf<MediaPlayer>()
    private var lastScreamTime = 0L
    
    fun playBeetleScream() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScreamTime < 100) return
        lastScreamTime = currentTime
        
        val player = MediaPlayer.create(context, R.raw.beetle_scream)
        player?.let {
            it.setVolume(0.5f, 0.5f)
            it.setOnCompletionListener { completedPlayer ->
                completedPlayer.release()
                beetleScreamPlayers.remove(completedPlayer)
            }
            beetleScreamPlayers.add(it)
            it.start()
        }
    }
    
    fun playBonusSound() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
    }
    
    fun release() {
        toneGenerator.release()
        beetleScreamPlayers.forEach { it.release() }
        beetleScreamPlayers.clear()
    }
}