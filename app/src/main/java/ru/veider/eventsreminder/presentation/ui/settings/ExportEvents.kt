package ru.veider.eventsreminder.presentation.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventDto
import ru.veider.eventsreminder.repo.Repo
import ru.veider.eventsreminder.usecases.toEventData
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.Charset

class ExportEvents {
	companion object {
		fun saveEventsToFile(uri: Uri, context: Context, data: List<EventDto>): Boolean {
			var res = false
			var output: ObjectOutputStream? = null
			try {
				output = ObjectOutputStream(
					context.contentResolver.openOutputStream(uri)
				)
				output.writeObject(Gson().toJson(data))
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

		fun loadEventsFromFile(uri: Uri, context: Context, repo: Repo): Boolean {
			var res = false
			var input: ObjectInputStream? = null
			try {
				input = ObjectInputStream(context.contentResolver.openInputStream(uri))
				val str = input.readObject() as String
				val entries = Gson().fromJson(str, Array<EventDto>::class.java).toList()
				CoroutineScope(Dispatchers.IO).launch {
					try {
						repo.clearAllLocalEvents()
						entries.map{it.toEventData()}.forEach { event ->
							repo.addLocalEvent(event)
						}
						res = true
					} catch (e:Exception){
						//context.outputError(e)
					}

				}
			} catch (e: FileNotFoundException) {
				Log.e(this::class.java.toString(), "", e)
			} catch (e: IOException) {
				Log.e(this::class.java.toString(), "", e)
			} catch (e: ClassNotFoundException) {
				Log.e(this::class.java.toString(), "", e)
			} catch (e: Exception) {
				Log.e(this::class.java.toString(), "", e)
			} finally {
				input?.close()
			}
			return res
		}
	}
}