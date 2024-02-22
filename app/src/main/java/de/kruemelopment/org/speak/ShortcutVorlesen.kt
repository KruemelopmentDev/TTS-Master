package de.kruemelopment.org.speak

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Objects

class ShortcutVorlesen : Activity() {
    private var tts: TextToSpeech? = null
    private var ready = 0
    private var speechrate = 0
    private var pitch = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) == null) return
        val action = Objects.requireNonNull(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT))
            .toString()
        val sp2 = getSharedPreferences("Settings", 0)
        speechrate = sp2.getInt("Speak", 10)
        pitch = sp2.getInt("Pitch", 10)
        tts = TextToSpeech(this) { i: Int ->
            if (i == TextToSpeech.SUCCESS) {
                ready = 1
                if (action.isNotEmpty()) {
                    var speed = speechrate / 10.0f
                    var ptchh = pitch / 10.0f
                    if (speed == 0.0f) {
                        speed = 0.1f
                    }
                    if (ptchh == 0.0f) {
                        ptchh = 0.1f
                    }
                    tts!!.setSpeechRate(speed)
                    tts!!.setPitch(ptchh)
                    tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {}
                        override fun onDone(utteranceId: String) {
                            finishAndRemoveTask()
                        }

                        override fun onError(utteranceId: String) {
                            finishAndRemoveTask()
                        }
                    })
                    tts!!.speak(action, TextToSpeech.QUEUE_FLUSH, null, "vorlesen")
                }
            } else {
                MyToast(this@ShortcutVorlesen).showError("Initialisierung fehlgeschlagen...")
            }
        }
    }
}
