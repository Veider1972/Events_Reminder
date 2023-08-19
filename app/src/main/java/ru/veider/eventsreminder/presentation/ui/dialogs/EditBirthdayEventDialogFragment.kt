package ru.veider.eventsreminder.presentation.ui.dialogs

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.databinding.EditBirthdayEventDialogFragmentBinding
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.presentation.ui.MAX_YEAR
import ru.veider.eventsreminder.usecases.addBirthDayEventFromLocalEdit
import java.time.LocalDate

class EditBirthdayEventDialogFragment : AbsDaggerDialogFragment() {
	private val binding: EditBirthdayEventDialogFragmentBinding by viewBinding()


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return try {
			dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_round_corner_background)
			inflater.inflate(R.layout.edit_birthday_event_dialog_fragment, container, false)
		} catch (t: Throwable) {
			logAndToast(t)
			null
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		try {
			processBundleArguments()
			var sourceId = 0L
			with(binding) {
				noYearCheckbox.setOnCheckedChangeListener { _, isChecked ->
					onNoYearCheckChanged(isChecked)
				}
				(inputBirthdaySpinnerPicker as ViewGroup).findViewById<View>(
					Resources.getSystem().getIdentifier("year", "id", "android")
				).visibility = GONE
				eventData?.let { eventData ->
					applyExistingEventData(eventData)
					sourceId = eventData.sourceId
				}
				negativeBtnCreateBirthdayEvent.setOnClickListener {
					try {
						findNavController().navigateUp()
					} catch (t: Throwable) {
						logAndToast(t)
					}
				}
				positiveBtnCreateBirthdayEvent.setOnClickListener {
					onPositiveBtnClicked(sourceId)
				}
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun EditBirthdayEventDialogFragmentBinding.onPositiveBtnClicked(
		sourceId: Long
	) {
		try {
			if (inputNameBirthdayEditText.text.trim().isEmpty()) {
				Toast.makeText(
					requireContext(),
					getString(R.string.toast_msg_create_birthday_dialog),
					Toast.LENGTH_SHORT
				).show()
			} else {
				saveEvent(sourceId)
				navigateOnSuccess()
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun EditBirthdayEventDialogFragmentBinding.onNoYearCheckChanged(
		isChecked: Boolean
	) {
		try {
			if (isChecked) {
				inputBirthdaySpinnerPicker.updateDate(
					MAX_YEAR,
					inputBirthdayCalendarPicker.month,
					inputBirthdayCalendarPicker.dayOfMonth
				)
				inputBirthdaySpinnerPicker.visibility = VISIBLE
				inputBirthdayCalendarPicker.visibility = GONE
			} else {
				inputBirthdayCalendarPicker.updateDate(
					if (inputBirthdaySpinnerPicker.year > LocalDate.now().year)
						LocalDate.now().year
					else
						inputBirthdaySpinnerPicker.year,
					inputBirthdaySpinnerPicker.month,
					inputBirthdaySpinnerPicker.dayOfMonth
				)
				inputBirthdayCalendarPicker.visibility = VISIBLE
				inputBirthdaySpinnerPicker.visibility = GONE
			}
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	override fun getSuccessIdToNavigate(sourceNavigationId: Int) =
		when (sourceNavigationId) {
			R.id.myEventsFragment -> R.id.action_editBirthdayDialog_to_myEvents
			R.id.dashboardFragment -> R.id.action_editBirthdayDialog_to_homeToDashboard
			else -> sourceNavigationId
		}

	private fun EditBirthdayEventDialogFragmentBinding.saveEvent(
		sourceId: Long
	) {
		try {
			val inputBirthdayDatePicker = if (noYearCheckbox.isChecked)
				inputBirthdaySpinnerPicker else inputBirthdayCalendarPicker

			dashboardViewModel.addLocalEvent(
				addBirthDayEventFromLocalEdit(
					inputNameBirthdayEditText.text.toString(),
					inputBirthdayDatePicker.dayOfMonth,
					inputBirthdayDatePicker.month + 1,
					if (noYearCheckbox.isChecked) null else
						inputBirthdayDatePicker.year,
					settings.minutesForStartNotification,
					sourceId
				)
			)
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun EditBirthdayEventDialogFragmentBinding.applyExistingEventData(
		eventData: EventData
	) {
		try {
			inputNameBirthdayEditText.setText(eventData.name)

			if ((eventData.birthday?.year ?: MAX_YEAR) == MAX_YEAR) {
				binding.noYearCheckbox.isChecked = true
				inputBirthdaySpinnerPicker.init(
					eventData.birthday?.year ?: eventData.date.year,
					eventData.date.monthValue - 1, eventData.date.dayOfMonth,
					null
				)
			} else
				inputBirthdayCalendarPicker.init(
					eventData.birthday?.year ?: eventData.date.year,
					eventData.date.monthValue - 1, eventData.date.dayOfMonth,
					null
				)
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}
}