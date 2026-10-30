package com.example.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEffectsEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var activeAudioTrack: AudioTrack? = null

    // Audio recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordedAudioBuffer = MutableStateFlow<ByteArray?>(null)
    val recordedAudioBuffer = _recordedAudioBuffer.asStateFlow()

    private val _audioWaveformLevels = MutableStateFlow<List<Float>>(emptyList())
    val audioWaveformLevels = _audioWaveformLevels.asStateFlow()

    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun triggerHapticFeedback() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundEffectsEngine", "Haptic error", e)
        }
    }

    fun playSoundEffect(effectId: String) {
        triggerHapticFeedback()
        scope.launch {
            try {
                activeAudioTrack?.stop()
                activeAudioTrack?.release()

                val sampleRate = 22050
                val pcmData = generatePcmForEffect(effectId, sampleRate)
                playPcmData(pcmData, sampleRate)
            } catch (e: Exception) {
                Log.e("SoundEffectsEngine", "Playback error", e)
            }
        }
    }

    fun playRecordedAudioWithEffect(pitchMultiplier: Float, speedMultiplier: Float, isRobot: Boolean) {
        triggerHapticFeedback()
        val rawBuffer = recordedAudioBuffer.value ?: return
        scope.launch {
            try {
                activeAudioTrack?.stop()
                activeAudioTrack?.release()

                val processedPcm = processPcmAudio(rawBuffer, pitchMultiplier, speedMultiplier, isRobot)
                val sampleRate = (22050 * speedMultiplier).toInt().coerceIn(8000, 48000)

                playPcmData(processedPcm, sampleRate)
            } catch (e: Exception) {
                Log.e("SoundEffectsEngine", "Voice change error", e)
            }
        }
    }

    fun startRecordingMicrophone(): Boolean {
        if (_isRecording.value) return true

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SoundEffectsEngine", "RECORD_AUDIO permission not granted")
            return false
        }

        _isRecording.value = true
        _recordedAudioBuffer.value = null

        recordingJob = scope.launch {
            val sampleRate = 22050
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            val bufferSize = minBufferSize.coerceAtLeast(4096)
            var audioRecord: AudioRecord? = null

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    _isRecording.value = false
                    return@launch
                }

                audioRecord.startRecording()
                val baos = ByteArrayOutputStream()
                val tempBuffer = ByteArray(bufferSize)
                val levels = mutableListOf<Float>()

                while (_isRecording.value) {
                    val readBytes = audioRecord.read(tempBuffer, 0, tempBuffer.size)
                    if (readBytes > 0) {
                        baos.write(tempBuffer, 0, readBytes)

                        var maxAmp = 0
                        for (i in 0 until readBytes - 1 step 2) {
                            val sample = (tempBuffer[i].toInt() and 0xFF) or (tempBuffer[i + 1].toInt() shl 8)
                            val absSample = abs(sample.toShort().toInt())
                            if (absSample > maxAmp) maxAmp = absSample
                        }
                        val normLevel = (maxAmp / 32768f).coerceIn(0.1f, 1.0f)
                        levels.add(normLevel)
                        if (levels.size > 25) levels.removeAt(0)
                        _audioWaveformLevels.value = levels.toList()
                    }
                    delay(30)
                }

                audioRecord.stop()
                _recordedAudioBuffer.value = baos.toByteArray()
            } catch (e: Exception) {
                Log.e("SoundEffectsEngine", "Mic record error", e)
            } finally {
                audioRecord?.release()
                _isRecording.value = false
            }
        }
        return true
    }

    fun stopRecordingMicrophone() {
        _isRecording.value = false
    }

    private fun playPcmData(pcmData: ByteArray, sampleRate: Int) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

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
            .setBufferSizeInBytes(Math.max(pcmData.size, minBufferSize))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcmData, 0, pcmData.size)
        activeAudioTrack = track
        track.play()
    }

    private fun processPcmAudio(
        rawPcm: ByteArray,
        pitchMult: Float,
        speedMult: Float,
        isRobot: Boolean
    ): ByteArray {
        val samples = ShortArray(rawPcm.size / 2)
        for (i in samples.indices) {
            val low = rawPcm[i * 2].toInt() and 0xFF
            val high = rawPcm[i * 2 + 1].toInt() shl 8
            samples[i] = (low or high).toShort()
        }

        val outputSize = (samples.size / pitchMult).toInt().coerceAtLeast(100)
        val processedSamples = ShortArray(outputSize)

        for (i in processedSamples.indices) {
            val sourceIdx = (i * pitchMult).toInt().coerceIn(0, samples.size - 1)
            var valSample = samples[sourceIdx].toInt()

            if (isRobot) {
                val modulation = sin(2.0 * PI * i * 160.0 / 22050.0)
                valSample = (valSample * (0.5 + 0.5 * modulation)).toInt()
            }

            processedSamples[i] = valSample.coerceIn(-32768, 32767).toShort()
        }

        val result = ByteArray(processedSamples.size * 2)
        for (i in processedSamples.indices) {
            val s = processedSamples[i].toInt()
            result[i * 2] = (s and 0xFF).toByte()
            result[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
        }
        return result
    }

    private fun generatePcmForEffect(effectId: String, sampleRate: Int): ByteArray {
        val durationSec = when (effectId) {
            "rimshot" -> 1.0
            "laugh_track" -> 2.2
            "sad_violin" -> 2.6
            "dramatic_horn" -> 1.8
            "boing" -> 1.1
            "fart" -> 0.8
            "wrong_buzzer" -> 1.0
            "win_chime" -> 1.8
            "cheetori" -> 1.4
            "cheer" -> 2.0
            "ey_jan" -> 1.5
            "kheili_ali" -> 1.5
            else -> 1.2
        }

        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)

        when (effectId) {
            "rimshot" -> { // Ba-Dum-Tss Comedy Drum Hit!
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    var sampleVal = 0.0

                    // First punch at 0.0s (Ba)
                    if (t < 0.25) {
                        val snareT = t
                        val drum = sin(2 * PI * 180.0 * exp(-snareT * 15) * snareT)
                        val noise = (Random.nextDouble() * 2 - 1) * exp(-snareT * 20)
                        sampleVal += (drum * 0.7 + noise * 0.5)
                    }

                    // Second punch at 0.25s (Dum)
                    if (t in 0.25..0.5) {
                        val snareT = t - 0.25
                        val drum = sin(2 * PI * 150.0 * exp(-snareT * 15) * snareT)
                        val noise = (Random.nextDouble() * 2 - 1) * exp(-snareT * 20)
                        sampleVal += (drum * 0.8 + noise * 0.4)
                    }

                    // Cymbal Crash at 0.5s (Tss!)
                    if (t >= 0.5) {
                        val cymbalT = t - 0.5
                        val highMetallic = sin(2 * PI * 4200.0 * cymbalT) * 0.3 + sin(2 * PI * 6800.0 * cymbalT) * 0.2
                        val hissNoise = (Random.nextDouble() * 2 - 1) * exp(-cymbalT * 4.0)
                        sampleVal += (highMetallic + hissNoise) * 0.9
                    }

                    pcm[i] = (sampleVal * 22000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "laugh_track" -> { // Vocalized rhythmic laughing track (Ha ha ha!)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    // 5 laugh bursts per second
                    val burstIndex = (t * 5.0).toInt()
                    val burstT = (t * 5.0) - burstIndex
                    val laughAmp = if (burstT < 0.7) sin(PI * burstT / 0.7) else 0.0

                    val basePitch = 380.0 + (burstIndex % 3) * 40.0
                    // Dual formant harmonics for vocal vowel "A/Ha"
                    val v1 = sin(2 * PI * basePitch * t)
                    val v2 = 0.6 * sin(2 * PI * (basePitch * 2.2) * t)
                    val v3 = 0.3 * sin(2 * PI * (basePitch * 3.4) * t)
                    val breath = (Random.nextDouble() * 2 - 1) * 0.15

                    pcm[i] = ((v1 + v2 + v3 + breath) * laughAmp * 24000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "dramatic_horn" -> { // Cinematic suspense brass crescendo (Dun Dun Duuun!)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val chordRoot = if (t < 0.4) 146.8 else if (t < 0.8) 130.8 else 110.0 // D -> C -> A
                    val brass1 = sin(2 * PI * chordRoot * t)
                    val brass2 = 0.6 * sin(2 * PI * (chordRoot * 1.5) * t) // Fifth
                    val brass3 = 0.4 * sin(2 * PI * (chordRoot * 2.0) * t) // Octave
                    val brass4 = 0.3 * sin(2 * PI * (chordRoot * 3.0) * t)
                    val env = (1.0 - exp(-t * 8)) * exp(-t * 0.7)

                    pcm[i] = ((brass1 + brass2 + brass3 + brass4) * env * 20000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "sad_violin" -> { // Minor dramatic vibrato violin
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val vibrato = 1.0 + 0.03 * sin(2 * PI * 6.0 * t)
                    val noteFreq = (392.0 - t * 45.0) * vibrato
                    val saw = (sin(2 * PI * noteFreq * t) +
                            0.5 * sin(2 * PI * noteFreq * 2 * t) +
                            0.3 * sin(2 * PI * noteFreq * 3 * t) +
                            0.2 * sin(2 * PI * noteFreq * 4 * t))
                    val bowFriction = (Random.nextDouble() * 2 - 1) * 0.08
                    val env = (1.0 - exp(-t * 5)) * exp(-t * 0.5)

                    pcm[i] = ((saw + bowFriction) * env * 18000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "boing" -> { // Cartoon spring bounce (Pat & Mat!)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val sweep = 200.0 + (t * 800.0) + sin(2 * PI * 25.0 * t) * 80.0
                    val harmonic = sin(2 * PI * sweep * t) + 0.4 * sin(2 * PI * sweep * 2.0 * t)
                    val env = exp(-t * 2.5)
                    pcm[i] = (harmonic * env * 22000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "wrong_buzzer" -> { // Game show harsh red buzzer
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val f1 = 120.0
                    val f2 = 126.0 // Dissonant beating
                    val buzz = (if (sin(2 * PI * f1 * t) > 0) 1.0 else -1.0) +
                               (if (sin(2 * PI * f2 * t) > 0) 0.8 else -0.8)
                    val env = exp(-t * 1.5)
                    pcm[i] = (buzz * env * 16000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "win_chime" -> { // Sparkling victory harp arpeggio
                val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // C5, E5, G5, C6, E6
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val noteIdx = (t * 3.5).toInt().coerceIn(0, notes.size - 1)
                    val noteT = t - (noteIdx / 3.5)
                    val freq = notes[noteIdx]
                    val chime = sin(2 * PI * freq * t) + 0.4 * sin(2 * PI * freq * 2 * t)
                    val env = exp(-noteT * 4.0)
                    pcm[i] = (chime * env * 20000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "fart" -> { // Comical cartoon raspberry fart
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val flutter = 65.0 + sin(2 * PI * 18.0 * t) * 35.0 + Random.nextDouble() * 25.0
                    val noise = (Random.nextDouble() * 2 - 1) * 0.4
                    val sample = (sin(2 * PI * flutter * t) + noise) * exp(-t * 3.0)
                    pcm[i] = (sample * 25000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "cheer" -> { // Clapping crowd applause
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val clapClust = (Random.nextDouble() * 2 - 1) * (0.5 + 0.5 * sin(2 * PI * 8.0 * t))
                    val whistle = 0.2 * sin(2 * PI * 1800.0 * exp(-t) * t)
                    val env = if (t < 0.3) t / 0.3 else exp(-(t - 0.3) * 1.5)
                    pcm[i] = ((clapClust + whistle) * env * 20000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            "cheetori" -> { // Jovial dual Persian fanfare
                val notes = doubleArrayOf(587.33, 659.25, 783.99, 880.0) // D, E, G, A
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val idx = (t * 3.0).toInt().coerceIn(0, notes.size - 1)
                    val freq = notes[idx]
                    val synth = sin(2 * PI * freq * t) + 0.5 * sin(2 * PI * freq * 1.5 * t)
                    val env = exp(-(t % 0.33) * 4.0)
                    pcm[i] = (synth * env * 22000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            else -> { // Rich melodic jingle
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 440.0 + sin(2 * PI * 5.0 * t) * 80.0
                    val tone = sin(2 * PI * freq * t) + 0.3 * sin(2 * PI * freq * 2 * t)
                    val env = exp(-t * 2.0)
                    pcm[i] = (tone * env * 20000).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
        }

        val result = ByteArray(pcm.size * 2)
        for (i in pcm.indices) {
            val s = pcm[i].toInt()
            result[i * 2] = (s and 0xFF).toByte()
            result[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
        }
        return result
    }
}
