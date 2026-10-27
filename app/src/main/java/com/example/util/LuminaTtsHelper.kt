package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LuminaTtsHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale("ru", "RU"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale.US)
                    }
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })
                    isReady = true
                }
            } else {
                Log.e("LuminaTTS", "TTS initialization failed")
            }
        }
    }

    fun speak(text: String) {
        if (!isReady || tts == null) return
        stop()
        _isSpeaking.value = true
        // Read clean text up to ~4000 characters
        val clean = text.replace(Regex("[#*`•\\[\\]()]"), " ").take(4000)
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "LuminaUtteranceId")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun toggleSpeak(text: String) {
        if (_isSpeaking.value) {
            stop()
        } else {
            speak(text)
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
