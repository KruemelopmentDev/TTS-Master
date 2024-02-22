package de.kruemelopment.org.speak

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat

class CopyService : Service(), OnPrimaryClipChangedListener {
    private var windowManager: WindowManager? = null
    private var icon: Ripple? = null
    private var icon2: ImageView? = null
    private var handler: Handler? = null
    private var r: Runnable? = null
    private var noview = false
    private var lasttext = ""
    private var clipboard: ClipboardManager? = null
    private var view: FrameLayout? = null
    private var myParams: WindowManager.LayoutParams? = null
    private var infromright: Animation? = null
    private var scaleDown: ObjectAnimator? = null
    private var sp2: SharedPreferences? = null
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == null) {
            showNotification(true)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) initBelowAndroidQ()
        } else if (intent.action == "stop") {
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (ignored: Exception) {
            }
            stopSelf()
        } else if (intent.action == "anzeigen") {
            show()
            showNotification(false)
        } else if (intent.action == "ausblenden") {
            hide()
            showNotification(true)
        }
        return START_STICKY
    }

    private fun initBelowAndroidQ() {
        init()
        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            icon2!!,
            PropertyValuesHolder.ofFloat("scaleX", 1.0f),
            PropertyValuesHolder.ofFloat("scaleY", 1.0f)
        )
        scaleUp.setDuration(200)
        scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            icon2!!,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        )
        scaleDown!!.setDuration(200)
        scaleDown!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                scaleUp.start()
                scaleUp.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        scaleDown!!.startDelay = 800
                        scaleDown!!.start()
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        assert(clipboard != null)
        clipboard!!.addPrimaryClipChangedListener(this)
        handler = Handler(Looper.getMainLooper())
        r = Runnable {
            val outtoright: Animation = TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f
            )
            outtoright.duration = 300
            outtoright.interpolator = AccelerateInterpolator()
            icon!!.startAnimation(outtoright)
            outtoright.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    windowManager!!.removeViewImmediate(view)
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun doclick(text: String) {
        val intent = Intent(applicationContext, Popup::class.java)
        intent.putExtra("text", text)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showNotification(show: Boolean) {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel("kanal", "Service", importance)
        mNotificationManager.createNotificationChannel(mChannel)
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val stop = Intent(applicationContext, CopyService::class.java)
        stop.setAction("stop")
        val sstop = PendingIntent.getService(
            this, 0,
            stop, PendingIntent.FLAG_IMMUTABLE
        )
        val open = Intent(applicationContext, Popup::class.java)
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val oopen = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE)
        val zeigen = Intent(applicationContext, CopyService::class.java)
        if (show) zeigen.setAction("anzeigen") else zeigen.setAction("ausblenden")
        val zzeigen = PendingIntent.getService(
            this, 0,
            zeigen, PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, "kanal")
        builder
            .setColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, theme))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setColorized(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round))
            .setSmallIcon(R.drawable.notifyicon)
            .setContentTitle("TTS Master")
            .setContentText("Zum Vorlesen tippen aktiviert")
        builder.addAction(NotificationCompat.Action(0, "Stoppen", sstop))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (show) builder.addAction(
                NotificationCompat.Action(
                    0,
                    "Anzeigen",
                    zzeigen
                )
            ) else builder.addAction(NotificationCompat.Action(0, "Ausblenden", zzeigen))
        }
        builder.addAction(NotificationCompat.Action(0, "Öffnen", oopen))
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else startForeground(1, builder.build())
    }

    override fun onDestroy() {
        if (clipboard != null) clipboard!!.removePrimaryClipChangedListener(this)
        if (windowManager != null && view != null) windowManager!!.removeViewImmediate(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onPrimaryClipChanged() {
        if (clipboard!!.primaryClip == null) return
        val charSequence = clipboard!!.primaryClip!!.getItemAt(0).text.toString()
        if (charSequence.isEmpty()) return
        if (StringBuffer(lasttext).reverse().toString() == charSequence) return
        lasttext = charSequence
        if (noview) {
            noview = false
            return
        }
        windowManager!!.addView(view, myParams)
        icon!!.startAnimation(infromright)
        infromright!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                icon!!.startRippleAnimation()
                scaleDown!!.start()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        view!!.setOnTouchListener(object : OnTouchListener {
            private var initialY = 0
            private var initialTouchY = 0f
            private var touchStartTime: Long = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (MotionEvent.ACTION_DOWN == event.action) {
                    initialY = myParams!!.y
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                }
                if (MotionEvent.ACTION_UP == event.action) {
                    val endtime = System.currentTimeMillis()
                    if (endtime - touchStartTime < 300) {
                        handler!!.removeCallbacks(r!!)
                        windowManager!!.removeViewImmediate(view)
                        doclick(charSequence)
                    } else handler!!.postDelayed(r!!, 1500)
                    val e = sp2!!.edit()
                    e.putInt("position", initialY + (event.rawY - initialTouchY).toInt())
                    e.apply()
                }
                if (event.action == MotionEvent.ACTION_MOVE) {
                    myParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager!!.updateViewLayout(v, myParams)
                    handler!!.removeCallbacks(r!!)
                }
                return true
            }
        })
        handler!!.postDelayed(r!!, 5000)
    }

    private fun init() {
        sp2 = getSharedPreferences("Einstellungen", 0)
        val position = sp2!!.getInt("position", -1)
        infromright = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, +1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        infromright!!.duration = 300
        infromright!!.interpolator = AccelerateInterpolator()
        val dm = Resources.getSystem().displayMetrics
        val width = dm.widthPixels
        val groese = (width * 0.4).toInt()
        view = FrameLayout(applicationContext)
        icon = Ripple(applicationContext, groese.toFloat())
        icon!!.gravity = Gravity.CENTER_VERTICAL
        icon!!.foregroundGravity = Gravity.CENTER_VERTICAL
        icon2 = ImageView(applicationContext)
        icon2!!.setImageResource(R.mipmap.ic_launcher_round)
        val layoutParams =
            RelativeLayout.LayoutParams((groese * 0.6).toInt(), (groese * 0.6).toInt())
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
        layoutParams.rightMargin = -groese / 10
        icon!!.addView(icon2, layoutParams)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        myParams =
            WindowManager.LayoutParams(
                groese,
                groese,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        if (position == -1) myParams!!.gravity = Gravity.CENTER_VERTICAL or Gravity.END else {
            myParams!!.gravity = Gravity.END
            myParams!!.y = position
        }
        view!!.addView(icon)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        init()
        windowManager!!.addView(view, myParams)
        icon!!.startAnimation(infromright)
        view!!.setOnTouchListener(object : OnTouchListener {
            private var initialY = 0
            private var initialTouchY = 0f
            private var touchStartTime: Long = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (MotionEvent.ACTION_DOWN == event.action) {
                    initialY = myParams!!.y
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                }
                if (MotionEvent.ACTION_UP == event.action) {
                    val endtime = System.currentTimeMillis()
                    if (endtime - touchStartTime < 300) {
                        val intent = Intent(applicationContext, Popup::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    val e = sp2!!.edit()
                    e.putInt("position", initialY + (event.rawY - initialTouchY).toInt())
                    e.apply()
                }
                if (event.action == MotionEvent.ACTION_MOVE) {
                    myParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager!!.updateViewLayout(v, myParams)
                }
                return true
            }
        })
    }

    private fun hide() {
        val outtoright: Animation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, +1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        outtoright.duration = 300
        outtoright.interpolator = AccelerateInterpolator()
        icon!!.startAnimation(outtoright)
        outtoright.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                windowManager!!.removeViewImmediate(view)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }
}