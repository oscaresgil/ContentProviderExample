package com.example.oscar.contentprovider

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import android.content.ContentValues
import android.net.Uri
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickAddName(view: View) {
         // Add a new student record
        val values = ContentValues()
        values.put(StudentsProvider.NAME, editText2.text.toString())

        values.put(StudentsProvider.GRADE, editText3.text.toString())

        val uri = contentResolver.insert(StudentsProvider.CONTENT_URI, values)

        Toast.makeText(baseContext, uri!!.toString(), Toast.LENGTH_LONG).show()
    }

    fun onClickRetrieveStudents(view:View) {
        // Retrieve student records
        val URL = "content://com.example.oscar.contentprovider.StudentsProvider"

        val students = Uri.parse(URL)
        val c = contentResolver.query(students, null, null, null, "name")

        if (c.moveToFirst()) {
            do {
                Toast.makeText(this,
                    c.getString(c.getColumnIndex(StudentsProvider._ID)) +
                    ", " + c.getString(c.getColumnIndex(StudentsProvider.NAME)) +
                    ", " + c.getString(c.getColumnIndex(StudentsProvider.GRADE)),
                Toast.LENGTH_SHORT).show()
            } while (c.moveToNext())
        }
        c.close();
    }
}
