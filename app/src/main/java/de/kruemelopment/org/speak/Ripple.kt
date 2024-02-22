package de.kruemelopment.org.speak

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat

@SuppressLint("ViewConstructor")
class Ripple(context: Context, hoehe: Float) : RelativeLayout(context) {
    private var paint: Paint? = null
    private var animatorSet: AnimatorSet? = null
    private var al: Animator.AnimatorListener? = null
    private var rippleView: RippleView? = null

    init {
        init(hoehe, context)
    }

    private fun init(oldhoehe: Float, context: Context) {
        if (isInEditMode) return
        val rippleColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, context.theme)
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.style = Paint.Style.FILL
        paint!!.color = rippleColor
        val hoehe = (oldhoehe / 1.5).toFloat()
        val rippleParams = LayoutParams(hoehe.toInt(), hoehe.toInt())
        rippleParams.addRule(CENTER_VERTICAL)
        rippleParams.addRule(ALIGN_PARENT_END)
        rippleParams.rightMargin = -(oldhoehe / 10).toInt()
        animatorSet = AnimatorSet()
        animatorSet!!.interpolator = AccelerateDecelerateInterpolator()
        val animatorList = ArrayList<Animator>()
        rippleView = RippleView(getContext())
        addView(rippleView, rippleParams)
        val scaleXAnimator = ObjectAnimator.ofFloat(rippleView!!, "ScaleX", 1.0f, 1.5f)
        scaleXAnimator.setDuration(500)
        animatorList.add(scaleXAnimator)
        val scaleYAnimator = ObjectAnimator.ofFloat(rippleView!!, "ScaleY", 1.0f, 1.5f)
        scaleYAnimator.setDuration(500)
        animatorList.add(scaleYAnimator)
        val alphaAnimator = ObjectAnimator.ofFloat(rippleView!!, "Alpha", 0.8f, 0f)
        alphaAnimator.setDuration(500)
        animatorList.add(alphaAnimator)
        animatorSet!!.playTogether(animatorList)
        al = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                Handler(Looper.getMainLooper()).postDelayed({ animatorSet!!.start() }, 700)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        }
    }

    private inner class RippleView(context: Context?) : View(context) {
        init {
            this.visibility = INVISIBLE
        }

        override fun onDraw(canvas: Canvas) {
            val radius = width.coerceAtMost(height) / 2
            canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paint!!)
        }
    }

    fun startRippleAnimation() {
        rippleView!!.visibility = VISIBLE
        animatorSet!!.addListener(al)
        animatorSet!!.start()
    }
}