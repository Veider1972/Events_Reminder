package ru.veider.eventsreminder.presentation.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ExportSettings {
	companion object {
		fun saveSharedPreferencesToFile(uri: Uri, context: Context, prefs: SharedPreferences): Boolean {
			var res = false
			var output: ObjectOutputStream? = null
			try {
				output = ObjectOutputStream(
					context.contentResolver.openOutputStream(uri)
				)
				output.writeObject(prefs.getAll())
				res = true
			} catch (e: FileNotFoundException) {
				Log.e(this::class.java.toString(), "", e)
			} catch (e: IOException) {
				Log.e(this::class.java.toString(), "", e)
			} finally {
				output?.flush()
				output?.close()
			}
			return res
		}

		fun loadSharedPreferencesFromFile(uri: Uri, context: Context, prefs: SharedPreferences): Boolean {
			var res = false
			var input: ObjectInputStream? = null
			try {
				input = ObjectInputStream(context.contentResolver.openInputStream(uri))
				val prefEdit: SharedPreferences.Editor =
					prefs.edit()
				prefEdit.clear()
				val entries = input.readObject() as Map<String, *>
				for (entry in entries) {
					val v: Any? = entry.value
					val key: String = entry.key
					if (v is Boolean) prefEdit.putBoolean(
						key,
						v
					) else if (v is Float) prefEdit.putFloat(
						key,
						v
					) else if (v is Int) prefEdit.putInt(
						key,
						v
					) else if (v is Long) prefEdit.putLong(
						key,
						v
					) else if (v is String) prefEdit.putString(key, v)
				}
				prefEdit.apply()
				res = true
			} catch (e: FileNotFoundException) {
				Log.e(this::class.java.toString(), "", e)
			} catch (e: IOException) {
				Log.e(this::class.java.toString(), "", e)
			} catch (e: ClassNotFoundException) {
				Log.e(this::class.java.toString(), "", e)
			} finally {
				input?.close()
			}
			return res
		}
	}
}