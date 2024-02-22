package de.kruemelopment.org.speak

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import de.kruemelopment.org.speak.WordManagementAdapter.MyViewHolder
import java.util.Locale

class WordManagementAdapter(
    private val context: Context,
    private val rowItems: MutableList<WordManagementList>,
    private val myDB: DataBaseHelper,
    private val adapterChanges: AdapterChanges,
    private val myToast: MyToast
) : RecyclerView.Adapter<MyViewHolder>() {
    private var rowItemsBackup: MutableList<WordManagementList>? = null
    private var tts: TextToSpeech?

    init {
        tts = TextToSpeech(context) { i: Int ->
            if (i != TextToSpeech.SUCCESS) {
                myToast.showError("Initialisierung fehlgeschlagen...")
                tts = null
            }
        }
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var titleTextView: TextView
        var constraintLayout: ConstraintLayout

        init {
            titleTextView = v.findViewById(R.id.textView009)
            constraintLayout = v.findViewById(R.id.relativ)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.listitem, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val item = rowItems[position]
        holder.titleTextView.text = item.text
        holder.constraintLayout.setOnClickListener {
            if (tts == null || item.id == null) return@setOnClickListener
            val speed: Float = ItemOneFragment.speechrate / 10.0f
            val ptchh: Float = ItemOneFragment.pitch / 10.0f
            tts!!.setSpeechRate(speed)
            tts!!.setPitch(ptchh)
            tts!!.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        holder.constraintLayout.setOnLongClickListener {
            if (item.id == null) return@setOnLongClickListener false
            val builder = AlertDialog.Builder(
                context
            )
            builder.setTitle("Aktion auswählen")
                .setMessage("Möchtest du diesen Text bearbeiten oder löschen?")
                .setPositiveButton("Löschen") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    myDB.deleteData(item.id)
                    rowItems.removeAt(position)
                    if (rowItemsBackup != null && rowItemsBackup!!.size > position) rowItemsBackup!!.removeAt(
                        position
                    )
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, rowItems.size)
                    if (rowItems.isEmpty()) adapterChanges.adapterIsEmpty()
                }
                .setNegativeButton("Bearbeiten") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    val dialog = Dialog(context, R.style.Dialog)
                    dialog.setContentView(R.layout.neues)
                    val editText = dialog.findViewById<TextInputEditText>(R.id.editText323)
                    editText.setText(item.text)
                    val save = dialog.findViewById<TextView>(R.id.textView5)
                    val cancel = dialog.findViewById<TextView>(R.id.textView8)
                    save.setOnClickListener {
                        if (editText.text == null) {
                            myToast.showError("Gib bitte einen Text ein!")
                            return@setOnClickListener
                        }
                        var help = editText.text.toString()
                        help = help.replace(" ", "")
                        help = help.replace("\n", "")
                        if (help.isNotEmpty()) {
                            myDB.updateData(item.id, editText.text.toString())
                            item.text = editText.text.toString()
                            rowItems[position] = item
                            if (rowItemsBackup != null && rowItemsBackup!!.size > position) rowItemsBackup!![position] =
                                item
                            notifyItemChanged(position)
                            myToast.showSuccess("Text erfolgreich geändert")
                            dialog.dismiss()
                        } else myToast.showError("Gib bitte einen Text ein!")
                    }
                    cancel.setOnClickListener { dialog.dismiss() }
                    dialog.show()
                }.show()
            false
        }
    }

    override fun getItemId(position: Int): Long {
        return rowItems[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return rowItems.size
    }

    fun initFilter() {
        rowItemsBackup = ArrayList(rowItems)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(charText: String) {
        val chart = charText.lowercase(Locale.getDefault())
        if (chart.isEmpty()) {
            rowItems.clear()
            rowItems.addAll(rowItemsBackup!!)
        } else {
            val help: MutableList<WordManagementList> = ArrayList()
            for (a in rowItemsBackup!!) {
                if (a.text.lowercase(Locale.getDefault()).contains(chart)) {
                    help.add(a)
                }
            }
            rowItems.clear()
            rowItems.addAll(help)
        }
        notifyDataSetChanged()
    }
}