package de.kruemelopment.org.speak

import android.app.Activity
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

class MyToast(context: Activity) {
    private val toast: Toast
    private val textView: TextView
    private val relativeLayout: RelativeLayout

    init {
        val layout = context.layoutInflater.inflate(R.layout.toast, context.findViewById(R.id.root))
        toast = Toast(context)
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 200)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        textView = layout.findViewById(R.id.toasttext)
        relativeLayout = layout.findViewById(R.id.root)
    }

    fun showSuccess(text: String?) {
        textView.text = text
        relativeLayout.setBackgroundResource(R.drawable.buttongreen)
        toast.show()
    }

    fun showError(text: String?) {
        textView.text = text
        relativeLayout.setBackgroundResource(R.drawable.buttonred)
        toast.show()
    }
}
