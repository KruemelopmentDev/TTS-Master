package de.kruemelopment.org.speak

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context?) : SQLiteOpenHelper(context, Database_Name, null, 1) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("Create Table $Table_Name (ID INTEGER PRIMARY KEY AUTOINCREMENT, Sprechen TEXT)")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $Table_Name")
    }

    fun insertData(sprechen: String?): Long {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("Sprechen", sprechen)
        val result = database.insert(Table_Name, null, contentValues)
        database.close()
        return result
    }

    fun updateData(id: String?, sprechen: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("Sprechen", sprechen)
        db.update(Table_Name, contentValues, "ID=?", arrayOf(id))
    }

    val allData: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery("Select * from $Table_Name", null)
        }

    fun deleteData(id: String?) {
        val db = this.writableDatabase
        db.delete(Table_Name, "ID=?", arrayOf(id))
    }

    companion object {
        var Database_Name = "Speak.db"
        var Table_Name = "default_table"
    }
}
