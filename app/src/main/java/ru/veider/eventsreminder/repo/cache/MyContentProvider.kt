package ru.veider.eventsreminder.repo.cache

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import android.widget.Toast


class MyContentProvider : ContentProvider() {
	private var mDbHelper: DbHelper? = null
	override fun onCreate(): Boolean {
		val context: Context? = context
		try {
			mDbHelper = DbHelper(context)
		} catch (t: Throwable) {
			logAndToast(t)
			return false
		}
		return true
	}

	override fun query(
		uri: Uri,
		projection: Array<out String>?,
		selection: String?,
		selectionArgs: Array<out String>?,
		sortOrder: String?
	): Cursor? {
		var retCursor: Cursor? = null
		try {
			val db: SQLiteDatabase? = mDbHelper?.readableDatabase
			when (sUriMatcher.match(uri)) {
				EVENTS_CODE -> retCursor = db?.query(
					Contract.TABLE_NAME,
					projection,
					selection,
					selectionArgs,
					null,
					null,
					sortOrder
				)

				else -> {}
			}
			context?.let {
				retCursor?.setNotificationUri(it.contentResolver, uri)
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
		return retCursor
	}

	override fun getType(uri: Uri): String? {
		return null
	}

	override fun insert(uri: Uri, values: ContentValues?): Uri? {
		var returnUri: Uri? = null
		try {
			mDbHelper?.let {
				val db: SQLiteDatabase = it.writableDatabase
				when (sUriMatcher.match(uri)) {
					EVENTS_CODE -> {
						val id = db.insert(Contract.TABLE_NAME, null, values)
						if (id > 0) {
							returnUri = ContentUris.withAppendedId(Contract.PATH_EVENTS_URI, id)
						}
					}

					else -> {}
				}
				context?.contentResolver?.notifyChange(uri, null)
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
		return returnUri
	}

	override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
		var res = 0
		try {
			mDbHelper?.let {
				val db: SQLiteDatabase = it.writableDatabase
				when (sUriMatcher.match(uri)) {
					EVENTS_CODE -> res = db.delete(Contract.TABLE_NAME, "1", null)

					else -> {}
				}
				context?.contentResolver?.notifyChange(uri, null)
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
		return res
	}

	override fun update(
		uri: Uri,
		values: ContentValues?,
		selection: String?,
		selectionArgs: Array<out String>?
	): Int {
		return 0
	}

	private fun logAndToast(t: Throwable) = logAndToast(t, this::class.java.toString())

	private fun logAndToast(t: Throwable, tag: String?) {
		try {
			Log.e(tag, "", t)
			Toast.makeText(context?.applicationContext, t.toString(), Toast.LENGTH_LONG).show()
		} catch (_: Throwable) {
		}
	}

	companion object {
		const val EVENTS_CODE = 100
		val sUriMatcher = buildUriMatcher()
		private fun buildUriMatcher(): UriMatcher {
			val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
			try {
				uriMatcher.addURI(Contract.AUTHORITY, Contract.PATH_EVENTS, EVENTS_CODE)
			} catch (t: Throwable) {
				Log.e(this::class.java.toString(), "", t)
			}
			return uriMatcher
		}

	}
}