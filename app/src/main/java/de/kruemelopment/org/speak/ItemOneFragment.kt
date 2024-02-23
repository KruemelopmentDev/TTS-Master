package de.kruemelopment.org.speak

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Objects

class ItemOneFragment : Fragment {
    private var tts: TextToSpeech? = null
    private var ready = 0
    private var format: String? = ".mp3"
    private var shared: String? = null
    private var dataBaseHelper: DataBaseHelper? = null
    private var myToast: MyToast? = null
    private var editText: TextInputEditText? = null

    constructor(shared: String?) : super() {
        this.shared = shared
    }

    constructor() : super()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        dataBaseHelper = DataBaseHelper(requireContext())
        myToast = MyToast(requireActivity())
        editText = view.findViewById(R.id.editText)
        if (shared != null) editText!!.setText(shared)
        val oben = view.findViewById<SeekBar>(R.id.seekBar)
        val unten = view.findViewById<SeekBar>(R.id.seekBar2)
        val sp2 = requireContext().getSharedPreferences("Settings", 0)
        oben.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBa: SeekBar, i: Int, b: Boolean) {
                val eder = sp2.edit()
                eder.putInt("Speak", seekBa.progress)
                eder.apply()
                speechrate = seekBa.progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        unten.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val ede = sp2.edit()
                ede.putInt("Pitch", seekBar.progress)
                ede.apply()
                pitch = seekBar.progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        speechrate = sp2.getInt("Speak", 10)
        pitch = sp2.getInt("Pitch", 10)
        oben.progress = speechrate
        unten.progress = pitch
        tts = TextToSpeech(context) { i: Int ->
            if (i == TextToSpeech.SUCCESS) {
                ready = 1
                if (shared != null && shared!!.isNotEmpty()) speakText(shared)
            } else {
                myToast!!.showError("Initialisierung fehlgeschlagen...")
            }
        }
        val readButton = view.findViewById<ExtendedFloatingActionButton>(R.id.button)
        readButton.setOnClickListener {
            if (editText!!.text == null || editText!!.text.toString().replace(" ", "")
                    .replace("\n", "").isEmpty()
            ) {
                myToast!!.showError("Gib erstmal was ein!")
            } else {
                val tt = editText!!.text.toString()
                if (ready == 1) {
                    speakText(tt)
                }
            }
        }
        val saveButton = view.findViewById<ExtendedFloatingActionButton>(R.id.button2)
        saveButton.setOnClickListener {
            if (editText!!.text == null || editText!!.text.toString().replace(" ", "")
                    .replace("\n", "").isEmpty()
            ) {
                myToast!!.showError("Gib bitte einen Text ein!")
            } else speichern(editText!!.text.toString(), false)
        }
        saveButton.setOnLongClickListener {
            val popupMenu = PopupMenu(requireContext(), saveButton)
            popupMenu.menuInflater.inflate(R.menu.dots, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                format = item.title as String?
                myToast!!.showSuccess("Sprüche werden jetzt in $format Dateien gespeichert")
                val sp3 = requireContext().getSharedPreferences("For", 0)
                val ede = sp3.edit()
                ede.putString("mat", format)
                ede.apply()
                false
            }
            popupMenu.show()
            false
        }
        val shareButton = view.findViewById<FloatingActionButton>(R.id.button5)
        shareButton.setOnClickListener {
            if (editText!!.text == null) {
                myToast!!.showError("Gib bitte einen Text ein!")
                return@setOnClickListener
            }
            val test = editText!!.text.toString()
            if (test.isNotEmpty()) {
                var speed = speechrate / 10.0f
                var ptchh = pitch / 10.0f
                if (speed == 0.0f) {
                    speed = 0.1f
                }
                if (ptchh == 0.0f) {
                    ptchh = 0.1f
                }
                val sp = requireContext().getSharedPreferences("For", 0)
                format = sp.getString("mat", ".mp3")
                storeaudio(test, "share$format", true, speed, ptchh)
            } else myToast!!.showError("Gib bitte einen Text ein!")
        }
        shareButton.setOnLongClickListener {
            if (editText!!.text == null || editText!!.text.toString().replace(" ", "")
                    .replace("\n", "").isEmpty()
            ) {
                myToast!!.showError("Gib bitte einen Text ein!")
            } else speichern(editText!!.text.toString(), true)
            true
        }
        val delete = view.findViewById<FloatingActionButton>(R.id.button6)
        delete.setOnClickListener { editText!!.setText("") }
        val settings = view.findViewById<FloatingActionButton>(R.id.button7)
        settings.setOnClickListener {
            val i = Intent()
            i.setAction("com.android.settings.TTS_SETTINGS")
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
        val add = view.findViewById<FloatingActionButton>(R.id.button8)
        add.setOnClickListener {
            if (editText!!.text == null) {
                myToast!!.showError("Gib bitte einen Text ein!")
                return@setOnClickListener
            }
            var help = editText!!.text.toString()
            help = help.replace(" ", "").replace("\n", "")
            if (help.isNotEmpty()) {
                val result = dataBaseHelper!!.insertData(editText!!.text.toString())
                if (result != -1L) myToast!!.showSuccess("Text erfolgreich gespeichert") else myToast!!.showError(
                    "Speichern fehlgeschlagen"
                )
            } else myToast!!.showError("Gib bitte einen Text ein!")
        }
    }

    private fun speichern(inputText: String?, share: Boolean) {
        val sp = requireContext().getSharedPreferences("For", 0)
        format = sp.getString("mat", ".mp3")
        val dialog = Dialog(requireContext(), R.style.Dialog)
        dialog.setContentView(R.layout.alertdia)
        val mInput = dialog.findViewById<TextInputEditText>(R.id.editText2)
        val btn = dialog.findViewById<TextView>(R.id.button3)
        val btn2 = dialog.findViewById<TextView>(R.id.button4)
        dialog.setCancelable(true)
        dialog.show()
        btn.setOnClickListener {
            val titel: String = if (mInput.text != null && mInput.text.toString().replace(" ", "")
                    .replace("\n", "").isNotEmpty()
            ) {
                mInput.text.toString()
            } else {
                val c = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
                sdf.format(c.time)
            }
            var speed = speechrate / 10.0f
            var ptchh = pitch / 10.0f
            if (speed == 0.0f) {
                speed = 0.1f
            }
            if (ptchh == 0.0f) {
                ptchh = 0.1f
            }
            storeaudio(inputText, titel + format, share, speed, ptchh)
            dialog.dismiss()
        }
        btn2.setOnClickListener { dialog.dismiss() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dots2, menu)
        val darkmode = menu.findItem(R.id.nightmode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            darkmode.setVisible(false)
        } else {
            darkmode.setChecked(MainActivity.nightmode)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item4 -> {
                val uri = Uri.parse("https://www.kruemelopment-dev.de/nutzungsbedingungen")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            R.id.item5 -> {
                val uri = Uri.parse("https://www.kruemelopment-dev.de/datenschutzerklärung")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            R.id.item6 -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.setData(Uri.parse("mailto:kontakt@kruemelopment-dev.de"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.nightmode -> {
                MainActivity.nightmode = !MainActivity.nightmode
                val sp = requireContext().getSharedPreferences("settings", 0)
                val e = sp.edit()
                e.putBoolean("mode", MainActivity.nightmode)
                e.apply()
                if (MainActivity.nightmode) AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                ) else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        return true
    }

    private fun speakText(text: String?) {
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
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun storeaudio(text: String?, filename: String, share: Boolean, speed: Float, pitch: Float) {
        val handler = Handler(Looper.getMainLooper())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            contentValues.put(
                MediaStore.MediaColumns.MIME_TYPE,
                "audio/" + format!!.replace(".", "")
            )
            contentValues.put(MediaStore.MediaColumns.TITLE, filename)
            contentValues.put(MediaStore.Audio.Media.ARTIST, "TTS Master")
            contentValues.put(
                MediaStore.MediaColumns.OWNER_PACKAGE_NAME,
                requireActivity().packageName
            )
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_MUSIC + File.separator + "TTS-Master"
            )
            val audioUri = requireContext().contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            val pfd: ParcelFileDescriptor?
            try {
                pfd = requireContext().contentResolver.openFileDescriptor(audioUri!!, "w")
                tts!!.setSpeechRate(speed)
                tts!!.setPitch(pitch)
                tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {}
                    override fun onDone(utteranceId: String) {
                        if (utteranceId == TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID) {
                            if (!share) handler.post { myToast!!.showSuccess("Gespeichert") } else {
                                val intent = Intent()
                                intent.setAction(Intent.ACTION_SEND)
                                intent.setType("audio/" + format!!.replace(".", ""))
                                intent.putExtra(Intent.EXTRA_STREAM, audioUri)
                                startActivity(Intent.createChooser(intent, "Datei teilen mit..."))
                            }
                        }
                    }

                    override fun onError(utteranceId: String) {
                        if (share) {
                            handler.post { myToast!!.showError("Teilen fehlgeschlagen...") }
                        } else {
                            handler.post { myToast!!.showError("Speichern fehlgeschlagen...") }
                        }
                    }
                })
                tts!!.synthesizeToFile(
                    text!!,
                    Bundle(),
                    pfd!!,
                    TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
                )
                pfd.close()
            } catch (e: FileNotFoundException) {
                if (share) myToast!!.showError("Teilen fehlgeschlagen...") else myToast!!.showError(
                    "Speichern fehlgeschlagen..."
                )
            }
        } else {
            val result = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (result == PackageManager.PERMISSION_GRANTED) {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                contentValues.put(
                    MediaStore.Audio.Media.MIME_TYPE,
                    "audio/" + format!!.replace(".", "")
                )
                contentValues.put(MediaStore.Audio.Media.TITLE, filename)
                contentValues.put(MediaStore.Audio.Media.ARTIST, "TTS Master")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(
                        MediaStore.Audio.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_MUSIC + File.separator + "TTS-Master"
                    )
                    contentValues.put(
                        MediaStore.Audio.Media.OWNER_PACKAGE_NAME,
                        requireActivity().packageName
                    )
                } else contentValues.put(
                    MediaStore.Audio.Media.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        .toString() + File.separator + "TTS-Master" + File.separator + filename
                )
                val audioUri = requireContext().contentResolver.insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                val testFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        .toString() + File.separator + "TTS-Master", filename
                )
                tts!!.setSpeechRate(speed)
                tts!!.setPitch(pitch)
                tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {}
                    override fun onDone(utteranceId: String) {
                        if (utteranceId == TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID) {
                            if (share) {
                                val share2 = Intent(Intent.ACTION_SEND)
                                share2.setType("audio/" + format!!.replace(".", ""))
                                share2.putExtra(Intent.EXTRA_STREAM, audioUri)
                                startActivity(Intent.createChooser(share2, "Datei teilen mit..."))
                            } else handler.post { myToast!!.showSuccess("Gespeichert") }
                        }
                    }

                    override fun onError(utteranceId: String) {
                        if (share) {
                            handler.post { myToast!!.showError("Teilen fehlgeschlagen...") }
                        } else {
                            handler.post { myToast!!.showError("Speichern fehlgeschlagen...") }
                        }
                    }
                })
                tts!!.synthesizeToFile(
                    text,
                    Bundle(),
                    testFile,
                    TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
                )
            } else if (share) {
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { result1: Boolean ->
                    if (result1) {
                        speichern(Objects.requireNonNull(editText!!.text).toString(), true)
                    } else {
                        val showRationale =
                            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        if (!showRationale) {
                            myToast!!.showError("Du hast uns dauerhaft das Recht entzogen nach der Speicherberechtigung zu fragen, ohne Speicherberechtigung können wir keine Audiodateien speichern!")
                        }
                    }
                }
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { result1: Boolean ->
                    if (result1) {
                        speichern(Objects.requireNonNull(editText!!.text).toString(), false)
                    } else {
                        val showRationale =
                            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        if (!showRationale) {
                            myToast!!.showError("Du hast uns dauerhaft das Recht entzogen nach der Speicherberechtigung zu fragen, ohne Speicherberechtigung können wir keine Audiodateien speichern!")
                        }
                    }
                }
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    companion object {
        var pitch = 0
        var speechrate = 0
    }
}