package ru.veider.eventsreminder.repo.cache

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class DbHelper(context: Context?) :
	SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	override fun onCreate(db: SQLiteDatabase) {
		try {
			SQL_CREATE_TABLE = "CREATE TABLE " +
					Contract.TABLE_NAME + "(" +
					Contract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					Contract.COL_EVENT_TYPE + " TEXT NOT NULL, " +
					Contract.COL_EVENT_PERIOD + " TEXT, " +
					Contract.COL_BIRTHDAY + " INTEGER, " +
					Contract.COL_EVENT_DATE + " INTEGER NOT NULL, " +
					Contract.COL_EVENT_TIME + " INTEGER, " +
					Contract.COL_TIME_NOTIFICATION + " INTEGER, " +
					Contract.COL_EVENT_TITLE + " TEXT NOT NULL, " +
					Contract.COL_EVENT_SOURCE_ID + " INTEGER NOT NULL, " +
					Contract.COL_EVENT_SOURCE_TYPE + " TEXT NOT NULL " +
					")"
			db.execSQL(SQL_CREATE_TABLE)
		} catch (t: Throwable) {
			logErr(t)
		}
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		try {
			SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + Contract.TABLE_NAME
			db.execSQL(SQL_DROP_TABLE)
			onCreate(db)
		} catch (t: Throwable) {
			logErr(t)
		}
	}

	companion object {
		private const val DATABASE_VERSION = 1
		private const val DATABASE_NAME = "eventItems.db"
		private var SQL_DROP_TABLE: String? = null
		private var SQL_CREATE_TABLE: String? = null
	}

	private fun logErr(t: Throwable) = logErr(t, this::class.java.toString())

	private fun logErr(t: Throwable, tag: String?) {
		try {
			Log.e(tag, "", t)
		} catch (_: Throwable) {
		}
	}
}