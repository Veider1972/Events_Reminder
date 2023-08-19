package ru.veider.eventsreminder.presentation.ui.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.databinding.DashboardRecyclerviewItemBinding
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventSourceType
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.presentation.MainActivity
import ru.veider.eventsreminder.presentation.ui.EVENT_ID
import ru.veider.eventsreminder.presentation.ui.findActivity
import ru.veider.eventsreminder.presentation.ui.toAgeInWordsByDate
import ru.veider.eventsreminder.presentation.ui.toDaysSinceNowInWords
import java.time.format.DateTimeFormatter
import java.util.Locale


class DashboardViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
	private val binding: DashboardRecyclerviewItemBinding by viewBinding()
	private val activity = view.context.findActivity() as MainActivity

	/**
	 * Привязать к вьюхолдеру конкретный EventData
	 * @param item EvenData для привязки
	 * @param isDataHeader Показывать ли в карточке Дату вверху карточки
	 * */
	fun bind(item: EventData, isDataHeader: Boolean) {
		try {
			with(binding) {
				when (item.type) {
					EventType.BIRTHDAY -> {
						setBirthdayEventSpecifics(item)
					}

					EventType.HOLIDAY -> {
						setHolidayEventSpecifics(item)
					}

					EventType.SIMPLE -> {
						setSimpleEventSpecifics(item)
					}
				}
				setCommonEventVisualisation(item, isDataHeader)
				root.setOnClickListener {
					setClickActions(item)
				}
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun setClickActions(item: EventData) {
		try {
			when (item.sourceType) {
				EventSourceType.LOCAL ->
					onLocalItemClicked(item)

				EventSourceType.CONTACTS ->
					onContactsItemClicked(item)

				EventSourceType.CALENDAR ->
					onCalendarItemClicked(item)
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun onCalendarItemClicked(item: EventData) {
		try {
			val intent = Intent(Intent.ACTION_VIEW)
			intent.data = Uri.parse(
				"content://com.android.calendar/events/" + java.lang.String.valueOf(
					item.sourceId
				)
			)
			intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
					or Intent.FLAG_ACTIVITY_SINGLE_TOP
					or Intent.FLAG_ACTIVITY_CLEAR_TOP
					or Intent.FLAG_ACTIVITY_NO_HISTORY
					or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
			activity.startActivity(intent)
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun onContactsItemClicked(item: EventData) {
		try {
			val intent = Intent(Intent.ACTION_VIEW)
			val contactUri = Uri.withAppendedPath(
				ContactsContract.Contacts.CONTENT_URI,
				java.lang.String.valueOf(item.sourceId)
			)
			intent.data = contactUri
			activity.startActivity(intent)
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun onLocalItemClicked(item: EventData) {
		try {
			val bundle = Bundle()
			bundle.putLong(
				EVENT_ID,
				item.sourceId
			)

			activity.findNavController(R.id.nav_host_fragment_activity_main)
				.navigate(R.id.myEventsFragment, bundle)
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun DashboardRecyclerviewItemBinding.setCommonEventVisualisation(
		item: EventData,
		isDataHeader: Boolean
	) {
		try {
			eventTitle.text = item.name.replaceFirstChar { it -> it.uppercase() }
			eventDaysTo.visibility =
				if (isDataHeader) {
					VISIBLE.also {
						eventDaysTo.text =
							item.date.toDaysSinceNowInWords()
					}
				} else GONE
			eventDate.visibility =
				if (isDataHeader) {
					VISIBLE.also {
						eventDate.text = item.date.format(
							DateTimeFormatter.ofPattern("dd.MM.yyyy, EEEE", Locale.getDefault())
						)
					}
				} else GONE
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun DashboardRecyclerviewItemBinding.setHolidayEventSpecifics(item: EventData) {
		try {
			itemContainer.setBackgroundColor(view.context.getColor(R.color.light_violet))
			if (item.sourceType == EventSourceType.LOCAL)
				eventImage.setImageResource(R.drawable.local_holiday_icon)
			else
				eventImage.setImageResource(R.drawable.holiday_icon_1)
			if (item.sourceType == EventSourceType.LOCAL) {
				item.time?.run {
					eventTime.text = this.format(DateTimeFormatter.ofPattern("HH:mm"))
					eventTime.isVisible = true
				}
			}
			eventDate.isVisible = false
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun DashboardRecyclerviewItemBinding.setSimpleEventSpecifics(
		item: EventData
	) {
		try {
			itemContainer.setBackgroundColor(view.context.getColor(R.color.light_blue))
			eventImage.setImageResource(
				if (item.sourceType == EventSourceType.LOCAL)
					R.drawable.local_simple_event_icon
				else
					R.drawable.simple_event_icon
			)
			item.time?.run {
				eventTime.text = this.format(DateTimeFormatter.ofPattern("HH:mm"))
				eventTime.isVisible = true
			}
			eventAge.isVisible = false
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun DashboardRecyclerviewItemBinding.setBirthdayEventSpecifics(
		item: EventData,
	) {
		try {
			itemContainer.setBackgroundColor(view.context.getColor(R.color.light_green))
			eventImage.setImageResource(
				if (item.sourceType == EventSourceType.LOCAL)
					R.drawable.local_birthday_icon
				else
					R.drawable.birthday_balloons
			)
			if (item.birthday != null && item.birthday.year != Int.MAX_VALUE && item.birthday <= item.date) {
					eventAge.text = view.context.getString(R.string.birthday_pattern, item.birthday.year)
				eventAge.isVisible = true
				eventTime.text = item.birthday.toAgeInWordsByDate(item.date)
				eventTime.isVisible = true
			} else {
				eventAge.isVisible = false
				eventTime.isVisible = false
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun logAndToast(t: Throwable) = logAndToast(t, this::class.java.toString())

	private fun logAndToast(t: Throwable, TAG: String) {
		try {
			Log.e(TAG, "", t)
			Toast.makeText(activity.applicationContext, t.toString(), Toast.LENGTH_LONG).show()
		} catch (_: Throwable) {
		}
	}
}