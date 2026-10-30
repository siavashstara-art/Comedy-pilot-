package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val persianLocale = Locale.forLanguageTag("fa")
            val result = tts?.setLanguage(persianLocale) ?: TextToSpeech.LANG_MISSING_DATA
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.getDefault()
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })

            _isInitialized.value = true
        } else {
            Log.e("TextToSpeechEngine", "TTS Init failed with status: $status")
        }
    }

    fun speak(text: String, pitch: Float = 1.0f, speechRate: Float = 1.0f) {
        val cleanText = text.replace(Regex("[\\p{So}\\p{Cn}\\p{Cs}]"), "").trim()
        if (cleanText.isBlank()) return

        if (_isInitialized.value) {
            try {
                tts?.setPitch(pitch)
                tts?.setSpeechRate(speechRate)
                _isSpeaking.value = true
                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "COMEDY_TTS_${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Log.e("TextToSpeechEngine", "Error speaking text", e)
                _isSpeaking.value = false
            }
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TextToSpeechEngine", "Error stopping TTS", e)
        }
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TextToSpeechEngine", "Error shutting down TTS", e)
        }
        tts = null
    }
}
