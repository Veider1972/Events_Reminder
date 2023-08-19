package ru.veider.eventsreminder.presentation.ui.myevents

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.databinding.MyEventsRecyclerviewItemBinding
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventSourceType
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.presentation.ui.findActivity
import ru.veider.eventsreminder.presentation.ui.toAgeInWordsByDate
import ru.veider.eventsreminder.presentation.ui.toDaysSinceNowInWords
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyEventsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
	private val binding: MyEventsRecyclerviewItemBinding by viewBinding()
	private val activity = view.context.findActivity()
	fun bind(
		item: EventData,
		isDataHeader: Boolean,
		isSelected : Boolean,
		editor: EventEditor
	) {
		try {
			with(binding) {
				setEventSpecificMarkup(item)
				setCommonEventVisualisation(item, isDataHeader)
					itemView.setOnClickListener {
					try {
						editor.openEditEvent(item)
					} catch (t: Throwable) {
						outputError(t)
					}
				}
//				myEventsRecyclerViewCardview.isSelected=isSelected
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun MyEventsRecyclerviewItemBinding.setEventSpecificMarkup(item: EventData) {
		try {
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
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun MyEventsRecyclerviewItemBinding.setCommonEventVisualisation(
		item: EventData,
		isDataHeader: Boolean
	) {
		try {
			eventTitle.text = item.name.replaceFirstChar { it->it.uppercase() }
			eventDaysTo.visibility =
				/*if (isDataHeader) {
					View.VISIBLE.also {
						eventDaysTo.text =
							item.date.toDaysSinceNowInWords()
					}
				} else */View.GONE
			eventDate.visibility =
				/*if (isDataHeader) {
					View.VISIBLE.also {
						eventDate.text = item.date.format(
							DateTimeFormatter.ofPattern("dd.MM.yyyy, EEEE", Locale.getDefault())
						)
					}
				} else */View.GONE
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun MyEventsRecyclerviewItemBinding.setHolidayEventSpecifics(item: EventData) {
		try {
			itemContainer.setBackgroundColor(view.context.getColor(R.color.light_violet))
			eventImage.setImageResource(R.drawable.local_holiday_icon)
			if (item.sourceType == EventSourceType.LOCAL){
				eventAge.text = item.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
				eventAge.isVisible = true

				if (item.time != null){
					eventTime.isVisible = true
					eventTime.text = item.time.format(DateTimeFormatter.ofPattern("HH:mm"))
				} else {
					eventTime.text = item.date.toAgeInWordsByDate(LocalDate.now())
					eventTime.isVisible = true
				}
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun MyEventsRecyclerviewItemBinding.setSimpleEventSpecifics(
		item: EventData
	) {
		try {
			itemContainer.setBackgroundColor(view.context.getColor(R.color.light_blue))
			eventImage.setImageResource(R.drawable.local_simple_event_icon)
			item.time?.run{
				eventTime.isVisible = true
				eventTime.text = this.format(DateTimeFormatter.ofPattern("HH:mm"))
			}
			item.date.run {
				eventAge.isVisible = true
				eventAge.text = this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun MyEventsRecyclerviewItemBinding.setBirthdayEventSpecifics(
		item: EventData,
	) {
		try {
			itemContainer.setBackgroundColor(view.context.getColor(R.color.light_green))
			eventImage.setImageResource(R.drawable.local_birthday_icon)
			eventAge.isVisible = true
			eventAge.text = item.birthday?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
			if (item.birthday != null && item.birthday.year != 0 && item.birthday <= item.date) {
				eventTime.text = item.birthday.toAgeInWordsByDate(item.date)
				eventTime.isVisible = true
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun outputError(t: Throwable) {
		try {
			Toast.makeText(activity.applicationContext, t.toString(), Toast.LENGTH_LONG).show()
			Log.e(this::class.java.toString(), "", t)
		} catch (_: Throwable) {
		}
	}
}