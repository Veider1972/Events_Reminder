package ru.veider.eventsreminder.presentation.ui.dialogs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.databinding.EditHolidayEventDialogFragmentBinding
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.PeriodType
import ru.veider.eventsreminder.usecases.addHolidayEventFromLocalEdit


class EditHolidayEventDialogFragment : AbsDaggerDialogFragment() {
    private val binding: EditHolidayEventDialogFragmentBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_round_corner_background)
            inflater.inflate(R.layout.edit_holiday_event_dialog_fragment, container, false)
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

                initSpinnerValues(holidayPeriodValueSpinner)

                eventData?.let { eventData ->
                    applyExistingEventData(eventData)
                    sourceId = eventData.sourceId
                }

                setTimePickerListeners(inputHolidayTime, requireContext(), isTimePickerEnabled)

                setPeriodPickerListeners(isHolidayPeriodEnabled,holidayPeriodValueSpinner)

                negativeBtnCreateHolidayEvent.setOnClickListener {
                    try {
                        findNavController().navigateUp()
                    } catch (t: Throwable) {
                        logAndToast(t)
                    }
                }
                positiveBtnCreateHolidayEvent.setOnClickListener {
                    onPositiveBtnClicked(sourceId)
                }
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    private fun EditHolidayEventDialogFragmentBinding.onPositiveBtnClicked(
        sourceId: Long
    ) {
        try {
            if (inputEventNameEditText.text.trim().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_msg_create_holiday_simple_dialog),
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

    override fun getSuccessIdToNavigate(sourceNavigationId: Int) =
        when (sourceNavigationId) {
            R.id.myEventsFragment -> R.id.action_editHolidayDialog_to_myEventsFragment
            R.id.dashboardFragment -> R.id.action_editHolidayDialog_to_dashboardFragment
            else -> sourceNavigationId
        }

    private fun EditHolidayEventDialogFragmentBinding.saveEvent(
        sourceId: Long
    ) {
        try {
            if (!isTimePickerEnabled.isChecked) {
                hours = null
                minutes = null
            }
            dashboardViewModel.addLocalEvent(
                addHolidayEventFromLocalEdit(
                    if (isHolidayPeriodEnabled.isChecked) selectedPeriod
                    else null,
                    inputEventNameEditText.text.toString(),
                    inputHolidayDatePicker.dayOfMonth,
                    inputHolidayDatePicker.month + 1,
                    inputHolidayDatePicker.year,
                    hours,
                    minutes,
                    settings.minutesForStartNotification,
                    sourceId
                )
            )
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    private fun EditHolidayEventDialogFragmentBinding.applyExistingEventData(
        eventData: EventData
    ) {
        try {
            inputEventNameEditText.setText(eventData.name)
            inputHolidayDatePicker.init(
                eventData.date.year,
                eventData.date.monthValue - 1, eventData.date.dayOfMonth,
                null
            )
            eventData.time?.let {
                isTimePickerEnabled.isChecked = true
                hours = it.hour
                minutes = it.minute
                inputHolidayTime.text = buildString {
                    append("%02d".format(hours))
                    append(":%02d".format(minutes))
                }
                inputHolidayTime.visibility = VISIBLE
            }

            eventData.period?.let {
                isHolidayPeriodEnabled.isChecked = true
                holidayPeriodValueSpinner.visibility = VISIBLE
                holidayPeriodValueSpinner.setSelection(PeriodType.values().indexOf(it))
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }
}