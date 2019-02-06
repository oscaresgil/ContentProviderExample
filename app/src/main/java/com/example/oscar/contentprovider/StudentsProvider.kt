package com.example.oscar.contentprovider

import java.util.HashMap

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher

import android.database.Cursor
import android.database.SQLException

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder

import android.net.Uri
import android.text.TextUtils

class StudentsProvider : ContentProvider() {

    /**
     * Database specific constant declarations
     */

    private var db: SQLiteDatabase? = null

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */

    private class DatabaseHelper internal constructor(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $STUDENTS_TABLE_NAME")
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = DatabaseHelper(context)

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */

        db = dbHelper.writableDatabase
        return db != null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        /**
         * Add a new student record
         */
        val rowID = db!!.insert(STUDENTS_TABLE_NAME, "", values)

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            val _uri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(_uri, null)
            return _uri
        }

        throw SQLException("Failed to add a record into $uri")
    }

    override fun query(
        uri: Uri, projection: Array<String>?,
        selection: String?, selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
//        @Suppress("NAME_SHADOWING") var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = STUDENTS_TABLE_NAME

        when (uriMatcher.match(uri)) {
            STUDENTS -> qb.setProjectionMap(STUDENTS_PROJECTION_MAP)

            STUDENT_ID -> qb.appendWhere(_ID + "=" + uri.pathSegments[1])
        }

        val c = qb.query(
            db, projection, selection,
            selectionArgs, null, null, sortOrder ?: NAME
        )
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        when (uriMatcher.match(uri)) {
            STUDENTS -> count = db!!.delete(STUDENTS_TABLE_NAME, selection, selectionArgs)

            STUDENT_ID -> {
                val id = uri.pathSegments[1]
                count = db!!.delete(
                    STUDENTS_TABLE_NAME, _ID + " = " + id +
                            if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "", selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun update(
        uri: Uri, values: ContentValues?,
        selection: String?, selectionArgs: Array<String>?
    ): Int {
        var count = 0
        when (uriMatcher.match(uri)) {
            STUDENTS -> count = db!!.update(STUDENTS_TABLE_NAME, values, selection, selectionArgs)

            STUDENT_ID -> count = db!!.update(
                STUDENTS_TABLE_NAME, values,
                _ID + " = " + uri.pathSegments[1] +
                        if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "", selectionArgs
            )
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? {
        when (uriMatcher.match(uri)) {
            /**
             * Get all student records
             */
            STUDENTS -> return "vnd.android.cursor.dir/vnd.example.students"
            /**
             * Get a particular student
             */
            STUDENT_ID -> return "vnd.android.cursor.item/vnd.example.students"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    companion object {
        internal val PROVIDER_NAME = "com.example.MyApplication.StudentsProvider"
        internal val URL = "content://$PROVIDER_NAME/students"
        internal val CONTENT_URI = Uri.parse(URL)

        internal val _ID = "_id"
        internal val NAME = "name"
        internal val GRADE = "grade"

        private val STUDENTS_PROJECTION_MAP: HashMap<String, String>? = null

        internal val STUDENTS = 1
        internal val STUDENT_ID = 2

        internal val uriMatcher: UriMatcher

        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher.addURI(PROVIDER_NAME, "students", STUDENTS)
            uriMatcher.addURI(PROVIDER_NAME, "students/#", STUDENT_ID)
        }

        internal val DATABASE_NAME = "College"
        internal val STUDENTS_TABLE_NAME = "students"
        internal val DATABASE_VERSION = 1
        internal val CREATE_DB_TABLE = " CREATE TABLE " + STUDENTS_TABLE_NAME +
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " name TEXT NOT NULL, " +
                " grade TEXT NOT NULL);"
    }
}