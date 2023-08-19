package ru.veider.eventsreminder.repo.remote

import android.content.ContentUris
import android.content.Context
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.provider.CalendarContract
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.domain.PeriodType
import ru.veider.eventsreminder.usecases.addEventFromCalendar
import java.time.LocalDate
import javax.inject.Inject

class PhoneCalendarRepoImpl @Inject constructor(
    val context: Context
) : PhoneCalendarRepo {
    override fun loadEventCalendar(endDay: Int): List<EventData> {
        val listCalendarDayEvents = arrayListOf<EventData>()
        try {
            val startDay = LocalDate.now()
            val calDate = Calendar.getInstance()
            calDate.timeZone = TimeZone.getDefault()
            calDate.set(startDay.year, startDay.monthValue - 1, startDay.dayOfMonth, 0, 0, 0)
            val start = calDate.timeInMillis
            calDate.add(Calendar.DAY_OF_MONTH, endDay)
            val end = calDate.timeInMillis
            val order = CalendarContract.Instances.BEGIN + " ASC"
            val eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon()
            ContentUris.appendId(eventsUriBuilder, start)
            ContentUris.appendId(eventsUriBuilder, end)
            val eventsUri = eventsUriBuilder.build()

            context.contentResolver.query(
                eventsUri,
                null,
                null,
                null,
                order
            )?.run {
                while (moveToNext()) {
                    val title =
                        getStringOrNull(getColumnIndex(CalendarContract.Instances.TITLE)).orEmpty()
                    val rrule =
                        getStringOrNull(getColumnIndex(CalendarContract.Instances.RRULE)).orEmpty()
                    val start =
                        if (rrule.isNullOrBlank()) getLongOrNull(getColumnIndex(CalendarContract.Instances.DTSTART))
                            ?: 0
                        else
                            getLongOrNull(getColumnIndex(CalendarContract.Instances.BEGIN))
                                ?: (getLongOrNull(getColumnIndex(CalendarContract.Instances.DTSTART))
                                    ?: 0)
                    val description =
                        getStringOrNull(getColumnIndex(CalendarContract.Instances.DESCRIPTION)).orEmpty()
                    var eventType = EventType.SIMPLE
                    val id = getLongOrNull(getColumnIndex(CalendarContract.Instances.EVENT_ID)) ?: 0
                    if (description.isNotEmpty()) {
                        eventType =
                            if (description.contains(context.getString(R.string.description_contains_birthday_repoimpl),true) ||
                                description.contains(context.getString(R.string.description_contains_day_of_birth_repoimpl),true)
                            )
                                EventType.BIRTHDAY
                            else EventType.HOLIDAY
                    }
                    listCalendarDayEvents.add(
                        addEventFromCalendar(
                            title, start, eventType, id,
                            if (eventType == EventType.BIRTHDAY) PeriodType.YEAR else null
                        )
                    )
                }
                close()
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
        return listCalendarDayEvents
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