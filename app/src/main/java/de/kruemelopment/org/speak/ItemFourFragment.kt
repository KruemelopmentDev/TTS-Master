package de.kruemelopment.org.speak

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.util.Objects

class ItemFourFragment : Fragment(), AdapterChanges {
    private var wordManagementAdapter: WordManagementAdapter? = null
    private val items: MutableList<WordManagementList> = ArrayList()
    private var myToast: MyToast? = null
    private var myDB: DataBaseHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.liste, container, false)
        setHasOptionsMenu(true)
        val recyclerView = v.findViewById<RecyclerView>(R.id.recyclerView)
        myToast = MyToast(requireActivity())
        myDB = DataBaseHelper(context)
        val res = myDB!!.allData
        if (res.count > 0) {
            while (res.moveToNext()) {
                items.add(WordManagementList(res.getString(1), res.getString(0)))
            }
        } else {
            items.add(WordManagementList("Du hast noch keine Texte gespeichert", null))
        }
        wordManagementAdapter =
            WordManagementAdapter(requireContext(), items, myDB!!, this, myToast!!)
        recyclerView.setHasFixedSize(false)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.recycleChildrenOnDetach = true
        recyclerView.layoutManager = linearLayoutManager
        val dividerItemDecoration = CustomDivider(recyclerView.context)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.adapter = wordManagementAdapter
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.eigenesprueche, menu)
        try {
            val item = menu.findItem(R.id.action_search)
            val searchView = item.actionView!!.findViewById<View>(R.id.searchview) as SearchView
            searchView.queryHint = "Suchen..."
            item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    wordManagementAdapter!!.initFilter()
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    wordManagementAdapter!!.filter("")
                    if (searchView.hasFocus()) {
                        val imm =
                            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                    }
                    return true
                }
            })
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    try {
                        val imm =
                            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        Objects.requireNonNull(imm)
                            .hideSoftInputFromWindow(requireView().windowToken, 0)
                    } catch (ignored: Exception) {
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    wordManagementAdapter!!.filter(newText)
                    return false
                }
            })
            searchView.setOnCloseListener {
                wordManagementAdapter!!.filter("")
                false
            }
        } catch (e: Exception) {
            myToast!!.showError("Die Suchfunktion konnte nicht initialisiert werden")
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu9) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Löschen bestätigen")
                .setMessage("Bist du dir sicher, dass du ALLES löschen willst? Alle Texte sind dann für immer weg...")
                .setPositiveButton("Ja") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    try {
                        requireContext().deleteDatabase("Speak.db")
                        val oldsize = items.size
                        items.clear()
                        wordManagementAdapter!!.notifyItemRangeRemoved(0, oldsize)
                        items.add(WordManagementList("Du hast noch keine Texte gespeichert", null))
                        wordManagementAdapter!!.notifyItemInserted(0)
                    } catch (e: Exception) {
                        myToast!!.showError("Da lief etwas schief")
                    }
                }
                .setNegativeButton("Nein") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .show()
        } else if (item.itemId == R.id.menu11) {
            val dialog = Dialog(requireContext(), R.style.DialogEditText)
            dialog.setContentView(R.layout.neues)
            val editText = dialog.findViewById<TextInputEditText>(R.id.editText323)
            val save = dialog.findViewById<TextView>(R.id.textView5)
            val cancel = dialog.findViewById<TextView>(R.id.textView8)
            save.setOnClickListener {
                if (editText.text == null) {
                    myToast!!.showError("Gib bitte einen Text ein!")
                    return@setOnClickListener
                }
                var help = editText.text.toString()
                help = help.replace(" ", "")
                help = help.replace("\n", "")
                if (help.isNotEmpty()) {
                    val result = myDB!!.insertData(editText.text.toString())
                    if (result != -1L) {
                        myToast!!.showSuccess("Text erfolgreich gespeichert")
                        items.add(WordManagementList(help, result.toString()))
                        if (items[0].id == null) {
                            items.removeAt(0)
                            wordManagementAdapter!!.notifyItemChanged(0)
                        } else wordManagementAdapter!!.notifyItemInserted(items.size - 1)
                    } else myToast!!.showError("Speichern fehlgeschlagen")
                    dialog.dismiss()
                } else myToast!!.showError("Gib bitte einen Text ein!")
            }
            cancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
        return true
    }

    override fun adapterIsEmpty() {
        items.clear()
        items.add(WordManagementList("Du hast noch keine Texte gespeichert", null))
        wordManagementAdapter!!.notifyItemInserted(0)
    }
}

interface AdapterChanges {
    fun adapterIsEmpty()
}