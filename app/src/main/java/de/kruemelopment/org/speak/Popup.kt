package de.kruemelopment.org.speak

import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.IOException

class Popup : AppCompatActivity() {
    private var speed = 0f
    private var ptchh = 0f
    private var myToast: MyToast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val sp2 = getSharedPreferences("settings", 0)
            val nightmode = sp2.getBoolean("mode", false)
            if (nightmode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)
        var text = intent.getStringExtra("text")
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (text == null) {
            text = if (clipboard.primaryClip != null) {
                clipboard.primaryClip!!.getItemAt(0).text.toString()
            } else ""
        }
        setContentView(R.layout.popuplayout)
        myToast = MyToast(this)
        val editText = findViewById<TextInputEditText>(R.id.editText2)
        val vorlesen = findViewById<FloatingActionButton>(R.id.button6)
        val teilen = findViewById<FloatingActionButton>(R.id.button5)
        editText.setText(text)
        val sp2 = getSharedPreferences("Settings", 0)
        val speechrate = sp2.getInt("Speak", 10)
        val sp33 = getSharedPreferences("Settings", 0)
        val pitch = sp33.getInt("Pitch", 10)
        val tts = TextToSpeech(this) { i: Int ->
            if (i != TextToSpeech.SUCCESS) {
                myToast!!.showError("Initialisierung fehlgeschlagen...")
                finishAndRemoveTask()
            }
        }
        speed = speechrate / 10.0f
        ptchh = pitch / 10.0f
        if (speed == 0.0f) {
            speed = 0.1f
        }
        if (ptchh == 0.0f) {
            ptchh = 0.1f
        }
        tts.setSpeechRate(speed)
        tts.setPitch(ptchh)
        vorlesen.setOnClickListener {
            if (editText.text == null) return@setOnClickListener
            tts.speak(editText.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        }
        teilen.setOnClickListener { v: View? ->
            val sp = getSharedPreferences("For", 0)
            val format = sp.getString("mat", ".mp3")
            if (editText.text == null) {
                myToast!!.showError("Quicksharing fehlgeschlagen...")
                return@setOnClickListener
            }
            try {
                val path = File(getExternalFilesDir(null), "share$format")
                if (!path.exists() && !path.createNewFile()) {
                    myToast!!.showError("Quicksharing fehlgeschlagen...")
                    return@setOnClickListener
                }
                tts.setSpeechRate(speed)
                tts.setPitch(ptchh)
                tts.synthesizeToFile(
                    editText.text.toString(),
                    Bundle(),
                    path,
                    TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
                )
                val uri = FileProvider.getUriForFile(
                    this@Popup,
                    BuildConfig.APPLICATION_ID + ".provider",
                    path
                )
                val share = Intent(Intent.ACTION_SEND)
                share.setType("audio/" + format!!.replace(".", ""))
                share.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(share, "Teilen mit..."))
            } catch (e: IOException) {
                myToast!!.showError("Quicksharing fehlgeschlagen...")
            }
        }
    }
}