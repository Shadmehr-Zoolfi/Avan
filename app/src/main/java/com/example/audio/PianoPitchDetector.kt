package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class DetectedNoteEvent(
    val midi: Int,
    val frequencyHz: Float,
    val noteName: String,
    val clarity: Float,
    val amplitudeRms: Float,
    val timestampMillis: Long = System.currentTimeMillis()
)

class PianoPitchDetector {

    private val sampleRate = 22050 // Optimized sample rate for low latency and piano fundamental frequencies (27.5 Hz - 4186 Hz)
    private val bufferSize = 2048 // ~92 ms window for accurate pitch detection down to low octaves

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _detectedNote = MutableSharedFlow<DetectedNoteEvent>(extraBufferCapacity = 16)
    val detectedNote: SharedFlow<DetectedNoteEvent> = _detectedNote.asSharedFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (_isListening.value) return

        try {
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val recordBufferSize = maxOf(minBufSize * 2, bufferSize * 2)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recordBufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _isListening.value = true

            recordingJob = scope.launch(Dispatchers.IO) {
                val audioBuffer = ShortArray(bufferSize)
                var lastMidiReported = -1
                var lastNoteTime = 0L

                while (isActive && _isListening.value) {
                    val readCount = audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
                    if (readCount > 0) {
                        // Calculate RMS amplitude for noise gate and visual meter
                        var sumSquare = 0.0
                        for (i in 0 until readCount) {
                            sumSquare += audioBuffer[i] * audioBuffer[i]
                        }
                        val rms = sqrt(sumSquare / readCount).toFloat() / Short.MAX_VALUE
                        _audioAmplitude.value = (rms * 4f).coerceIn(0f, 1f)

                        // Threshold to prevent picking up quiet background ambient noise
                        if (rms > 0.025f) {
                            val pitchResult = detectPitchYin(audioBuffer, readCount, sampleRate)
                            if (pitchResult != null && pitchResult.clarity > 0.65f) {
                                val midi = frequencyToMidi(pitchResult.frequency)
                                val currentTime = System.currentTimeMillis()

                                // Debounce note triggering slightly (min 90ms between same note triggers)
                                if (midi in 21..108) {
                                    if (midi != lastMidiReported || (currentTime - lastNoteTime > 180)) {
                                        lastMidiReported = midi
                                        lastNoteTime = currentTime

                                        val noteName = midiToName(midi)
                                        _detectedNote.emit(
                                            DetectedNoteEvent(
                                                midi = midi,
                                                frequencyHz = pitchResult.frequency,
                                                noteName = noteName,
                                                clarity = pitchResult.clarity,
                                                amplitudeRms = rms,
                                                timestampMillis = currentTime
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            stopListening()
        }
    }

    fun stopListening() {
        _isListening.value = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore on cleanup
        } finally {
            audioRecord = null
            _audioAmplitude.value = 0f
        }
    }

    private data class PitchResult(val frequency: Float, val clarity: Float)

    // Robust YIN-style difference function for monophonic pitch detection
    private fun detectPitchYin(buffer: ShortArray, size: Int, sampleRate: Int): PitchResult? {
        val halfSize = size / 2
        val yinBuffer = FloatArray(halfSize)

        // Step 1: Difference function
        for (tau in 0 until halfSize) {
            var sum = 0f
            for (i in 0 until halfSize) {
                val delta = (buffer[i] - buffer[i + tau]).toFloat()
                sum += delta * delta
            }
            yinBuffer[tau] = sum
        }

        // Step 2: Cumulative mean normalized difference function
        yinBuffer[0] = 1f
        var runningSum = 0f
        for (tau in 1 until halfSize) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum > 0f) {
                (yinBuffer[tau] * tau) / runningSum
            } else {
                1f
            }
        }

        // Step 3: Absolute threshold (find first dip below threshold)
        val threshold = 0.20f
        var tauEstimate = -1
        for (tau in 2 until halfSize) {
            if (yinBuffer[tau] < threshold) {
                // Find local minimum
                while (tau + 1 < halfSize && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    // Continue to valley bottom
                }
                tauEstimate = tau
                break
            }
        }

        if (tauEstimate == -1) {
            // Find global minimum if no valley below threshold was found
            var minVal = Float.MAX_VALUE
            var minTau = -1
            for (tau in 4 until halfSize) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau]
                    minTau = tau
                }
            }
            if (minVal < 0.45f) {
                tauEstimate = minTau
            }
        }

        if (tauEstimate > 0) {
            // Parabolic interpolation for fine frequency accuracy
            val x0 = if (tauEstimate > 0) tauEstimate - 1 else tauEstimate
            val x2 = if (tauEstimate + 1 < halfSize) tauEstimate + 1 else tauEstimate
            val s0 = yinBuffer[x0]
            val s1 = yinBuffer[tauEstimate]
            val s2 = yinBuffer[x2]

            val betterTau = if (s2 + s0 - 2 * s1 != 0f) {
                tauEstimate + (s2 - s0) / (2f * (2f * s1 - s2 - s0))
            } else {
                tauEstimate.toFloat()
            }

            val freq = sampleRate.toFloat() / betterTau
            val clarity = (1f - yinBuffer[tauEstimate]).coerceIn(0f, 1f)
            return PitchResult(freq, clarity)
        }

        return null
    }

    private fun frequencyToMidi(frequency: Float): Int {
        if (frequency <= 0) return 0
        val noteNum = 12.0 * (log2(frequency.toDouble() / 440.0)) + 69.0
        return noteNum.roundToInt()
    }

    private fun midiToName(midi: Int): String {
        val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val note = notes[midi % 12]
        val octave = (midi / 12) - 1
        return "$note$octave"
    }
}
