package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    var soundEnabled: Boolean = true
    var vibrationEnabled: Boolean = true
    var sfxVolume: Float = 1.0f

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playButtonClick() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 700.0, durationMs = 35, volume = 0.4f * sfxVolume)
        }
        vibrate(15)
    }

    fun playDiceRoll() {
        if (!soundEnabled) return
        scope.launch {
            val freqs = listOf(350.0, 480.0, 620.0, 520.0, 780.0)
            for (f in freqs) {
                playTone(f, 40, 0.5f * sfxVolume)
            }
        }
        vibrate(40)
    }

    fun playTokenMove() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 880.0, durationMs = 50, volume = 0.5f * sfxVolume)
        }
        vibrate(20)
    }

    fun playCapture() {
        if (!soundEnabled) return
        scope.launch {
            // Sci-fi strike power burst
            playTone(frequency = 440.0, durationMs = 60, volume = 0.8f * sfxVolume)
            playTone(frequency = 220.0, durationMs = 120, volume = 0.9f * sfxVolume)
            playTone(frequency = 110.0, durationMs = 150, volume = 0.7f * sfxVolume)
        }
        vibrate(120)
    }

    fun playSafeLanding() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 660.0, durationMs = 70, volume = 0.6f * sfxVolume)
            playTone(frequency = 990.0, durationMs = 90, volume = 0.7f * sfxVolume)
        }
        vibrate(30)
    }

    fun playTokenHome() {
        if (!soundEnabled) return
        scope.launch {
            val fanfare = listOf(523.25, 659.25, 783.99, 1046.50)
            for (f in fanfare) {
                playTone(f, 90, 0.7f * sfxVolume)
            }
        }
        vibrate(80)
    }

    fun playVictory() {
        if (!soundEnabled) return
        scope.launch {
            val melody = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            for (f in melody) {
                playTone(f, 130, 0.8f * sfxVolume)
            }
        }
        vibrate(250)
    }

    private fun vibrate(millis: Long) {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {
            // Graceful fallback on devices without vibrator permission or capability
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return

        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            // Sine wave with exponential fade out to avoid clicks
            val progress = i.toDouble() / numSamples
            val envelope = 1.0 - progress
            val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
            val value = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt()
            samples[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            // Release after playback
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Audio output unavailable or interrupted
        }
    }
}
