package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp
import kotlin.math.sin

class PianoAudioSynth {

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pre-calculated PCM cache for the standard piano octave range (MIDI 48 to 84, C3 to C6)
    private val soundCache = ConcurrentHashMap<Int, ShortArray>()

    @Volatile
    private var isInitialized = false

    init {
        scope.launch {
            warmUpCache()
        }
    }

    private fun warmUpCache() {
        // Pre-generate primary keys for immediate zero-lag response
        for (midi in 48..84) {
            soundCache[midi] = generatePianoWave(midi, durationSeconds = 1.6f)
        }
        isInitialized = true
    }

    private fun generatePianoWave(midi: Int, durationSeconds: Float): ShortArray {
        val totalSamples = (sampleRate * durationSeconds).toInt()
        val buffer = ShortArray(totalSamples)
        val frequency = 440.0 * Math.pow(2.0, (midi - 69.0) / 12.0)

        val twoPi = 2.0 * Math.PI

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate

            // Piano envelope: sharp percussive attack, followed by exponential decay
            val attackTime = 0.005 // 5ms attack
            val envelope = if (t < attackTime) {
                t / attackTime
            } else {
                exp(-3.2 * (t - attackTime))
            }

            // Piano harmonic spectrum (rich acoustic timbre with fundamental + harmonics)
            val h1 = sin(twoPi * frequency * t)
            val h2 = 0.55 * sin(twoPi * frequency * 2.0 * t) * exp(-1.2 * t)
            val h3 = 0.30 * sin(twoPi * frequency * 3.0 * t) * exp(-2.0 * t)
            val h4 = 0.15 * sin(twoPi * frequency * 4.0 * t) * exp(-3.0 * t)

            val sampleValue = (h1 + h2 + h3 + h4) * envelope * 0.45

            buffer[i] = (sampleValue.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    fun playNote(midi: Int, velocity: Float = 0.85f) {
        scope.launch(Dispatchers.IO) {
            try {
                val samples = soundCache.getOrPut(midi) {
                    generatePianoWave(midi, 1.5f)
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val bufferSize = maxOf(samples.size * 2, minBufferSize)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.setVolume(velocity.coerceIn(0.1f, 1.0f))
                track.write(samples, 0, samples.size)
                track.play()

                // Clean release track after note playback
                kotlinx.coroutines.delay(1600)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Ignore audio play errors on background thread
            }
        }
    }
}
