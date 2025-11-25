package com.hsl.wear.ui.voice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class VoiceInputHelper(private val activity: Activity) {

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null

    fun initialize() {
        if (hasPermission()) {
            setupSpeechRecognizer()
        }
    }

    fun startListening(
        prompt: String = "Speak location",
        language: String = "en-US"
    ) {
        if (!hasPermission()) {
            _error.value = "Microphone permission not granted"
            return
        }

        val recognizer = speechRecognizer ?: run {
            setupSpeechRecognizer()
            speechRecognizer
        } ?: run {
            _error.value = "Speech recognizer not available"
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            _isListening.value = true
            _error.value = null
            recognizer.startListening(intent)
        } catch (e: Exception) {
            _error.value = "Failed to start speech recognition: ${e.message}"
            _isListening.value = false
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        recognitionListener = null
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            _error.value = "Speech recognition not available on this device"
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)

        recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _error.value = null
            }

            override fun onBeginningOfSpeech() {
                // User started speaking
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed (could be used for visualization)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(errorCode: Int) {
                _isListening.value = false
                _error.value = getErrorMessage(errorCode)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()
                _recognizedText.value = text
                _isListening.value = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()
                // Could be used for real-time feedback if needed
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Other events
            }
        }

        speechRecognizer?.setRecognitionListener(recognitionListener)
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech input found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
            else -> "Unknown error: $errorCode"
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearRecognizedText() {
        _recognizedText.value = null
    }
}

@Composable
fun rememberVoiceInputHelper(activity: Activity): VoiceInputHelper {
    val voiceHelper = remember { VoiceInputHelper(activity) }

    DisposableEffect(Unit) {
        voiceHelper.initialize()
        onDispose {
            voiceHelper.destroy()
        }
    }

    return voiceHelper
}