package ru.veider.eventsreminder.repo.local

import android.util.Log
import ru.veider.eventsreminder.domain.EventData
import javax.inject.Inject

class LocalRepoImp @Inject constructor(
	private val localEventsDB: LocalEventsDB
) : LocalRepo {
	override fun addEvent(event: EventData) =
		try {
			localEventsDB
				.getLocalEventsDao()
				.insert(LocalEvent.fromEventData(event))
		} catch (t: Throwable) {
			logErr(t)
		}

	override fun deleteEvent(event: EventData) {
		try {
			localEventsDB.getLocalEventsDao()
				.delete(LocalEvent.fromEventData(event))
		} catch (t: Throwable) {
			logErr(t)
		}
	}

	override fun clear() {
		try {
			localEventsDB.getLocalEventsDao().clear()
		} catch (t: Throwable) {
			logErr(t)
		}
	}

	override fun getList(): List<EventData> {
		return try {
			localEventsDB
				.getLocalEventsDao()
				.getLocalEvents()
				.map { event -> event.toEventData() }
		} catch (t: Throwable) {
			logErr(t)
			listOf()
		}
	}

	private fun logErr(t: Throwable) = logErr(t, this::class.java.toString())

	private fun logErr(t: Throwable, TAG: String) {
		try {
			Log.e(TAG, "", t)
		} catch (_: Throwable) {
		}
	}
}