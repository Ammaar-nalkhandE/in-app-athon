package com.example.test_sample

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import java.util.Locale
import android.util.Log
import java.lang.reflect.Method

class MainActivity : FlutterActivity(), TextToSpeech.OnInitListener {
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var audioManager: AudioManager
    private var tts: TextToSpeech? = null
    private val CHANNEL = "com.example.silent_auto_call_picker/calls"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        tts = TextToSpeech(this, this)

        MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "initializeCallHandler") {
                initializeCallHandler()
                result.success("Call handler initialized")
            } else {
                result.notImplemented()
            }
        }
    }

    private fun initializeCallHandler() {
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        muteRingerAndPickCall(incomingNumber)
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        // Call is answered
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        // Call ended
                    }
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun muteRingerAndPickCall(incomingNumber: String?) {
        // Mute the ringer
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT

        // Convert call info to speech (Caller ID)
        if (incomingNumber != null) {
            speakCallerId(incomingNumber)
        }

        // Auto-pickup using ITelephony or AccessibilityService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use CallScreeningService for Android 10+
            Log.d("SilentCallPicker", "Attempting to pick call using CallScreeningService.")
            // Implement CallScreeningService to pick the call silently
        } else {
            Log.d("SilentCallPicker", "Attempting to pick call using ITelephony for older Android versions.")
            // For older versions, use ITelephony to answer the call
            try {
                val telephonyClass = Class.forName(telephonyManager.javaClass.name)
                val getITelephony: Method = telephonyClass.getDeclaredMethod("getITelephony")
                getITelephony.isAccessible = true
                val iTelephony = getITelephony.invoke(telephonyManager)
                val iTelephonyClass = Class.forName(iTelephony.javaClass.name)
                val answerRingingCall: Method = iTelephonyClass.getDeclaredMethod("answerRingingCall")
                answerRingingCall.invoke(iTelephony)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun speakCallerId(incomingNumber: String) {
        tts?.speak("Incoming call from $incomingNumber", TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
