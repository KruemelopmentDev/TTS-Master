package de.kruemelopment.org.speak

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var selectedFragment: Fragment? = null
    private var sharedtext: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val sp2 = getSharedPreferences("settings", 0)
            nightmode = sp2.getBoolean("mode", false)
            if (nightmode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        val intent = intent
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type != null) {
            sharedtext = intent.getStringExtra(Intent.EXTRA_TEXT)
        }
        handleintentfromshortcut(getIntent())
        setContentView(R.layout.bottomview)
        val sese = getSharedPreferences("Start", 0)
        val web = sese.getBoolean("agbs", false)
        if (!web) {
            val dialog = Dialog(this, R.style.Dialog)
            dialog.setContentView(R.layout.webdialog)
            val ja = dialog.findViewById<TextView>(R.id.textView5)
            val nein = dialog.findViewById<TextView>(R.id.textView8)
            ja.setOnClickListener {
                val ed = sese.edit()
                ed.putBoolean("agbs", true)
                ed.apply()
                dialog.dismiss()
                requestPermission()
            }
            nein.setOnClickListener { finishAndRemoveTask() }
            val textView = dialog.findViewById<TextView>(R.id.textView4)
            textView.text = Html.fromHtml(
                "Mit der Nutzung dieser App aktzeptiere ich die " +
                        "<a href=\"https://www.kruemelopment-dev.de/datenschutzerklaerung\">Datenschutzerklärung</a>" + " und die " + "<a href=\"https://www.kruemelopment-dev.de/nutzungsbedingungen\">Nutzungsbedingungen</a>" + " von Krümelopment Dev",HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            textView.movementMethod = LinkMovementMethod.getInstance()
            dialog.setCancelable(false)
            dialog.show()
        } else requestPermission()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigationView.selectedItemId = R.id.ite1
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.ite1) {
                if (selectedFragment !is ItemOneFragment) {
                    selectedFragment = ItemOneFragment(sharedtext)
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_layout, selectedFragment!!)
                    transaction.commit()
                    sharedtext = null
                }
            } else if (item.itemId == R.id.ite2) {
                if (selectedFragment !is ItemFourFragment) {
                    selectedFragment = ItemFourFragment()
                    val transactio = supportFragmentManager.beginTransaction()
                    transactio.replace(R.id.frame_layout, selectedFragment!!)
                    transactio.commit()
                }
            }
            false
        }
        selectedFragment = ItemOneFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, selectedFragment!!)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sammel: MutableList<String> = ArrayList()
        val myDB = DataBaseHelper(this)
        val res = myDB.allData
        if (res.count > 0) {
            while (res.moveToNext()) {
                sammel.add(res.getString(1))
            }
        }
        myDB.close()
        val drawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round) ?: return
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val icon = Icon.createWithBitmap(bmp)
        var shortcut: ShortcutInfo? = null
        var shortcut2: ShortcutInfo? = null
        val shortcut3: ShortcutInfo
        val shortcutManager = getSystemService(
            ShortcutManager::class.java
        )
        when (sammel.size) {
            2 -> {
                val intent2 = Intent(this, MainActivity::class.java)
                intent2.putExtra("wort", sammel[sammel.size - 2])
                intent2.setAction(Intent.ACTION_MAIN)
                shortcut2 = ShortcutInfo.Builder(this, "id2")
                    .setShortLabel(sammel[sammel.size - 2])
                    .setLongLabel(sammel[sammel.size - 2])
                    .setIcon(icon)
                    .setIntent(intent2)
                    .build()
                val intent3 = Intent(this, MainActivity::class.java)
                intent3.putExtra("wort", sammel[sammel.size - 1])
                intent3.setAction(Intent.ACTION_MAIN)
                shortcut3 = ShortcutInfo.Builder(this, "id3")
                    .setShortLabel(sammel[sammel.size - 1])
                    .setLongLabel(sammel[sammel.size - 1])
                    .setIcon(icon)
                    .setIntent(intent3)
                    .build()
            }

            1 -> {
                val intent3 = Intent(this, MainActivity::class.java)
                intent3.putExtra("wort", sammel[sammel.size - 1])
                intent3.setAction(Intent.ACTION_MAIN)
                shortcut3 = ShortcutInfo.Builder(this, "id3")
                    .setShortLabel(sammel[sammel.size - 1])
                    .setLongLabel(sammel[sammel.size - 1])
                    .setIcon(icon)
                    .setIntent(intent3)
                    .build()
            }

            else -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("wort", sammel[sammel.size - 3])
                intent.setAction(Intent.ACTION_MAIN)
                shortcut = ShortcutInfo.Builder(this, "id1")
                    .setShortLabel(sammel[sammel.size - 3])
                    .setLongLabel(sammel[sammel.size - 3])
                    .setIcon(icon)
                    .setIntent(intent)
                    .build()
                val intent2 = Intent(this, MainActivity::class.java)
                intent2.putExtra("wort", sammel[sammel.size - 2])
                intent2.setAction(Intent.ACTION_MAIN)
                shortcut2 = ShortcutInfo.Builder(this, "id2")
                    .setShortLabel(sammel[sammel.size - 2])
                    .setLongLabel(sammel[sammel.size - 2])
                    .setIcon(icon)
                    .setIntent(intent2)
                    .build()
                val intent3 = Intent(this, MainActivity::class.java)
                intent3.putExtra("wort", sammel[sammel.size - 1])
                intent3.setAction(Intent.ACTION_MAIN)
                shortcut3 = ShortcutInfo.Builder(this, "id3")
                    .setShortLabel(sammel[sammel.size - 1])
                    .setLongLabel(sammel[sammel.size - 1])
                    .setIcon(icon)
                    .setIntent(intent3)
                    .build()
            }
        }
        assert(shortcutManager != null)
        if (shortcut != null) shortcutManager!!.setDynamicShortcuts(
            listOf(
                shortcut,
                shortcut2,
                shortcut3
            )
        ) else if (shortcut2 != null) shortcutManager!!.setDynamicShortcuts(
            listOf(shortcut2, shortcut3)
        ) else shortcutManager!!.setDynamicShortcuts(
            listOf(shortcut3)
        )
    }

    private fun handleintentfromshortcut(intent: Intent) {
        try {
            val extra = intent.getStringExtra("wort")
            if (!extra.isNullOrEmpty()) {
                sharedtext = extra
                intent.removeExtra("wort")
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                14
            )
        }
    }

    companion object {
        var nightmode = false
    }
}
