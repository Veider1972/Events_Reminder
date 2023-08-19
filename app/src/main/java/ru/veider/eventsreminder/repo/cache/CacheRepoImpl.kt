package ru.veider.eventsreminder.repo.cache

import android.app.AlarmManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.widget.Toast
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventSourceType
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.domain.PeriodType
import ru.veider.eventsreminder.presentation.ui.toInt
import ru.veider.eventsreminder.presentation.ui.toLocalDate
import ru.veider.eventsreminder.presentation.ui.toLocalTime
import javax.inject.Inject

class CacheRepoImpl @Inject constructor(val context: Context) : CacheRepo {
    private var mCursor: Cursor? = null
    override fun getList(): List<EventData> {
        val eventsList = mutableListOf<EventData>()
        try {
            mCursor?.close()

            mCursor = context.applicationContext.contentResolver.query(
                Contract.PATH_EVENTS_URI,
                null,
                null,
                null,
                Contract._ID + " ASC"
            )
            mCursor?.let { cur ->
                while (cur.moveToNext()) {
                    try { // Для возможности обработки после исключения в одном из элементов
                        eventsList.add(
                            EventData(
                                EventType.valueOf(cur.getString(cur.getColumnIndexOrThrow(Contract.COL_EVENT_TYPE))),
                                cur.getStringOrNull(cur.getColumnIndexOrThrow(Contract.COL_EVENT_PERIOD))
                                    ?.let { PeriodType.fromString(it) },
                                cur.getIntOrNull(cur.getColumnIndexOrThrow(Contract.COL_BIRTHDAY))
                                    ?.toLocalDate(),
                                cur.getInt(cur.getColumnIndexOrThrow(Contract.COL_EVENT_DATE))
                                    .toLocalDate(),
                                cur.getInt(cur.getColumnIndexOrThrow(Contract.COL_EVENT_TIME))
                                    .toLocalTime(),
                                cur.getInt(cur.getColumnIndexOrThrow(Contract.COL_TIME_NOTIFICATION))
                                    .toLocalTime(),
                                cur.getString(cur.getColumnIndexOrThrow(Contract.COL_EVENT_TITLE)),
                                cur.getLong(cur.getColumnIndexOrThrow(Contract.COL_EVENT_SOURCE_ID)),
                                EventSourceType.valueOf(
                                    cur.getString(
                                        cur.getColumnIndexOrThrow(
                                            Contract.COL_EVENT_SOURCE_TYPE
                                        )
                                    )
                                )
                            )
                        )
                    } catch (t: Throwable) {
                        logAndToast(t)
                    }
                }
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
        return eventsList
    }

    override fun renew(events: List<EventData>) {
        try {
            context.applicationContext.contentResolver.delete(
                Contract.PATH_EVENTS_URI,
                null, null
            )
            events.forEach { addEventToCache(it) }

        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    private fun addEventToCache(eventData: EventData) {
        try {
            val values = ContentValues()
            values.put(Contract.COL_EVENT_TYPE, eventData.type.toString())
            values.put(Contract.COL_EVENT_PERIOD, eventData.period?.toString())
            values.put(Contract.COL_BIRTHDAY, eventData.birthday?.toInt())
            values.put(Contract.COL_EVENT_DATE, eventData.date.toInt())
            values.put(Contract.COL_EVENT_TIME, eventData.time?.toInt())
            values.put(Contract.COL_TIME_NOTIFICATION, eventData.timeNotifications?.toInt())
            values.put(Contract.COL_EVENT_TITLE, eventData.name)
            values.put(Contract.COL_EVENT_SOURCE_ID, eventData.sourceId)
            values.put(Contract.COL_EVENT_SOURCE_TYPE, eventData.sourceType.toString())
            context.applicationContext.contentResolver.insert(Contract.PATH_EVENTS_URI, values)
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    private fun logAndToast(t: Throwable) = logAndToast(t, this::class.java.toString())

    private fun logAndToast(t: Throwable, TAG: String) {
        try {
            Log.e(TAG, "", t)
            Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show()
        } catch (_: Throwable) {
        }
    }
}