package ru.veider.eventsreminder.widget


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.util.Log
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.preference.PreferenceManager
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.domain.EventSourceType
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.presentation.ui.MAX_YEAR
import ru.veider.eventsreminder.presentation.ui.toAgeInWordsByDate
import ru.veider.eventsreminder.presentation.ui.toInt
import ru.veider.eventsreminder.presentation.ui.toLocalDate
import ru.veider.eventsreminder.presentation.ui.toLocalTime
import ru.veider.eventsreminder.presentation.ui.toShortDaysSinceNowInWords
import ru.veider.eventsreminder.repo.cache.Contract
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject


class WidgetRemoteViewsFactory
@Inject
constructor(
	private val applicationContext: Context,
) :
	RemoteViewsService.RemoteViewsFactory {
	private var mCursor: Cursor? = null

	companion object {
		const val TAG = "ru.veider.eventsreminder.widget.WidgetRemoteViewsFactory"
	}

	override fun onCreate() {}
	override fun onDataSetChanged() {
		try {
			val prefs: SharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(applicationContext)
			val interval = prefs.getInt(
				applicationContext.getString(R.string.key_widget_interval_of_events_preference),
				365
			)
			mCursor?.close()
			val identityToken = Binder.clearCallingIdentity()
			val uri: Uri = Contract.PATH_EVENTS_URI
			val fromDate = LocalDate.now().toInt()
			val toDate = LocalDate.now().plusDays(interval.toLong()).toInt()

			mCursor = applicationContext.contentResolver.query(
				uri,
				null,
				Contract.COL_EVENT_DATE + " BETWEEN ? AND ?",
				arrayOf(fromDate.toString(), toDate.toString()),
				Contract.COL_EVENT_DATE + " ASC"
			)
			Binder.restoreCallingIdentity(identityToken)
		} catch (t: Throwable) {
			Log.e(TAG, null, t)
			throw t
		}
	}

	override fun onDestroy() {
		try {
			mCursor?.close()
		} catch (t: Throwable) {
			Log.e(TAG, null, t)
			throw t
		}
	}

	override fun getCount(): Int {
		return mCursor?.count ?: 0
	}

	override fun getViewAt(position: Int): RemoteViews? {
		try {
			if (position == AdapterView.INVALID_POSITION ||
				mCursor?.moveToPosition(position) != true
			) return null
			val rv = RemoteViews(applicationContext.packageName, R.layout.item_app_widget)
			/**
			 * Для работы кликов по элементам списка в AppWidget.onUpdate()
			 * требуется установить темплейт виджета
			 * widgetView.setPendingIntentTemplate(R.id.widgetList, clickPendingIntentTemplate)
			 */
			rv.setOnClickFillInIntent(
				R.id.itemAppWidget,
				Intent()
			)// Здесь достаточно пустого интента
			val prefs: SharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(applicationContext)
			val isShowingEventDate = prefs.getBoolean(
				applicationContext.getString(R.string.key_event_date_checkbox_preference),
				true
			)
			val isShowingEventTime = prefs.getBoolean(
				applicationContext.getString(R.string.key_event_time_checkbox_preference),
				true
			)
			val isShowingAge = prefs.getBoolean(
				applicationContext.getString(R.string.key_age_checkbox_preference),
				true
			)
			val sizeFontWidget = prefs.getInt(
				applicationContext.getString(R.string.key_widget_font_size_preference), 13
			).toFloat()
			val line1Color = prefs.getInt(
				applicationContext.getString(R.string.key_first_line_color_preference),
				ContextCompat.getColor(applicationContext, R.color.widget_first_line_default_color)
			)
			val line2Color = prefs.getInt(
				applicationContext.getString(R.string.key_second_line_color_preference),
				ContextCompat.getColor(applicationContext, R.color.widget_second_line_default_color)
			)
			val birthdayTextColor = prefs.getInt(
				applicationContext.getString(R.string.key_widget_birthday_font_color_preference),
				0x01579B
			)
			val holidayTextColor = prefs.getInt(
				applicationContext.getString(R.string.key_widget_holiday_font_color_preference),
				0x3700B3
			)
			val simpleEventTextColor = prefs.getInt(
				applicationContext.getString(R.string.key_widget_simple_event_font_color_preference),
				0x151414
			)
			if (position % 2 == 0) {
				rv.setInt(R.id.itemAppWidget, "setBackgroundColor", line1Color)
			} else {
				rv.setInt(R.id.itemAppWidget, "setBackgroundColor", line2Color)
			}
			mCursor?.let {
				val eventSource = it.getString(it.getColumnIndexOrThrow(Contract.COL_EVENT_SOURCE_TYPE))
				cleanupTextViews(rv)

				when (it.getString(it.getColumnIndexOrThrow(Contract.COL_EVENT_TYPE))) {
					EventType.SIMPLE.toString() -> {
						setWidgetLineParameters(
							rv,
							simpleEventTextColor,
							sizeFontWidget
						)
						bindTimeWithEventTimeTextView(isShowingEventTime, rv, it)
					}

					EventType.HOLIDAY.toString() -> {
						setWidgetLineParameters(
							rv,
							holidayTextColor,
							sizeFontWidget
						)
						bindTimeWithEventTimeTextView(
							isShowingEventTime &&
									eventSource == EventSourceType.LOCAL.toString(), rv, it
						)
					}

					EventType.BIRTHDAY.toString() -> {
						setWidgetLineParameters(
							rv,
							birthdayTextColor,
							sizeFontWidget
						)
						bindAgeWithWidgetAgeTextView(isShowingAge, rv, it)
					}
				}
				rv.setTextViewText(
					R.id.eventTitle,
					it.getString(it.getColumnIndexOrThrow(Contract.COL_EVENT_TITLE)).replaceFirstChar { it->it.uppercase() }
				)
				if (isShowingEventDate) {
					with(it.getInt(it.getColumnIndexOrThrow(Contract.COL_EVENT_DATE))) {
						val daysTo = ChronoUnit.DAYS.between(LocalDate.now(), this.toLocalDate()).toInt()
						rv.setTextViewText(R.id.daysToEvent, this.toLocalDate().toShortDaysSinceNowInWords())
						if (daysTo > 0) {
							val date = this.toLocalDate().format(DateTimeFormatter.ofPattern("(dd.MM)"))
							rv.setTextViewText(R.id.eventDate, date)
							rv.setViewVisibility(R.id.eventDate, View.VISIBLE)
						}
					}
				}
			}
			return rv
		} catch (t: Throwable) {
			Log.e(TAG, null, t)
			throw t
		}
	}

	private fun cleanupTextViews(rv: RemoteViews) {
		arrayOf(R.id.eventDate, R.id.eventTime, R.id.eventAge).forEach {
			rv.setViewVisibility(it, View.GONE)
			rv.setTextViewText(it, "")
		}
	}

	private fun bindAgeWithWidgetAgeTextView(
		isShowingAge: Boolean,
		rv: RemoteViews,
		it: Cursor
	) {
		try {
			if (isShowingAge) {
				it.getIntOrNull(it.getColumnIndexOrThrow(Contract.COL_BIRTHDAY))?.let { birthday ->
					val date = it.getInt(it.getColumnIndexOrThrow(Contract.COL_EVENT_DATE)).toLocalDate()
					val birthdate = birthday.toLocalDate()
					if (date >= birthdate && date.year != MAX_YEAR) {
						val age = "(${birthdate.toAgeInWordsByDate(date)}, ${birthdate.year})"
						rv.setTextViewText(R.id.eventAge, age)
						rv.setViewVisibility(R.id.eventAge, View.VISIBLE)
//						return
					} else {
						rv.setViewVisibility(R.id.eventAge, View.GONE)
					}
				}
			} else {
				rv.setViewVisibility(R.id.eventAge, View.GONE)
				rv.setTextViewText(R.id.eventAge, "")
			}
		} catch (t: Throwable) {
			Log.e(TAG, null, t)
			throw t
		}
	}

	private fun bindTimeWithEventTimeTextView(
		isShowingEventTime: Boolean,
		rv: RemoteViews,
		it: Cursor
	) {
		try {
			val time = it.getIntOrNull(it.getColumnIndexOrThrow(Contract.COL_EVENT_TIME))
			if (isShowingEventTime && time != null) {
				rv.setTextViewText(
					R.id.eventTime,
					time.toLocalTime().format(
						DateTimeFormatter.ofPattern("HH:mm")
					)
				)
				rv.setViewVisibility(R.id.eventTime, View.VISIBLE)
			} else rv.setViewVisibility(R.id.eventTime, View.GONE)
		} catch (t: Throwable) {
			Log.e(TAG, null, t)
			throw t
		}
	}

	private fun setWidgetLineParameters(rv: RemoteViews, color: Int, sizeFontWidget: Float) {
		try {
			with(rv) {
				arrayOf(R.id.daysToEvent, R.id.eventTitle, R.id.eventDate, R.id.eventTime, R.id.eventAge).forEach {
					setTextColor(it, color)
					setTextViewTextSize(it, COMPLEX_UNIT_SP, sizeFontWidget)
				}
			}
		} catch (t: Throwable) {
			Log.e(TAG, null, t)
			throw t
		}
	}

	override fun getLoadingView(): RemoteViews? {
		return null
	}

	override fun getViewTypeCount(): Int {
		return 1
	}

	override fun getItemId(position: Int): Long {
		try {
			mCursor?.let {
				if (it.moveToPosition(position)) return it.getLong(0)
			}
			return position.toLong()
		} catch (t: Throwable) {
			logErr(t)
			throw t
		}
	}

	override fun hasStableIds(): Boolean {
		return true
	}

	private fun logErr(t: Throwable) = logErr(t, this::class.java.toString())

	private fun logErr(t: Throwable, TAG: String) {
		try {
			Log.e(TAG, "", t)
		} catch (_: Throwable) {
		}
	}
}