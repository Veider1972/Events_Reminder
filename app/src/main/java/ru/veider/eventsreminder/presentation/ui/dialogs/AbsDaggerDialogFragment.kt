package ru.veider.eventsreminder.presentation.ui.dialogs

import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerDialogFragment
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.di.ViewModelFactory
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.PeriodType
import ru.veider.eventsreminder.domain.SettingsData
import ru.veider.eventsreminder.presentation.ui.SOURCE_ID_TO_NAVIGATE
import ru.veider.eventsreminder.presentation.ui.dashboard.DashboardViewModel
import ru.veider.eventsreminder.presentation.ui.parcelable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

abstract class AbsDaggerDialogFragment : DaggerDialogFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected val dashboardViewModel by viewModels<DashboardViewModel>({ this }) { viewModelFactory }

    @Inject
    lateinit var settings: SettingsData
    private var sourceIdToNavigate: Int? = null
    var eventData: EventData? = null
    protected var hours: Int? = null
    protected var minutes: Int? = null
    protected var selectedPeriod: PeriodType? = null

    protected abstract fun getSuccessIdToNavigate(sourceNavigationId: Int): Int

    protected fun processBundleArguments() {
        try {
            arguments?.parcelable<EventData>(EventData::class.toString())?.let { eventData = it }
            arguments?.getInt(SOURCE_ID_TO_NAVIGATE)?.let { sourceIdToNavigate = it }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    protected fun navigateOnSuccess() {
        try {
            findNavController().navigate(
                getSuccessIdToNavigate(
                    sourceIdToNavigate ?: R.id.dashboardFragment
                )
            )
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    protected fun initSpinnerValues(valueSpinner: Spinner) {
        try {
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.period_item,
                R.id.period,
                PeriodType.values().toList()
            )
            valueSpinner.adapter = adapter
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    protected fun setPeriodPickerListeners(enableSpinnerCheckBox: CheckBox, valueSpinner: Spinner) {
        try {
            valueSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        selectedPeriod = valueSpinner.adapter.getItem(position) as PeriodType?
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedPeriod = null
                    }
                }
            enableSpinnerCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    valueSpinner.visibility = View.VISIBLE
                } else valueSpinner.visibility = View.INVISIBLE
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    protected fun setTimePickerListeners(textView: TextView, context: Context, checkBox: CheckBox) {
        try {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                try {
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    hours = hour
                    minutes = minute
                    textView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
                    textView.visibility = View.VISIBLE
                } catch (t: Throwable) {
                    logAndToast(t)
                }
            }
            val timePickerDialog = TimePickerDialog(
                context, R.style.date_picker, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), true
            )

            timePickerDialog.setOnCancelListener {
                try {
                    if (textView.visibility == View.INVISIBLE) checkBox.isChecked = false
                } catch (t: Throwable) {
                    logAndToast(t)
                }
            }

            textView.setOnClickListener {
                try {
                    timePickerDialog.show()
                } catch (t: Throwable) {
                    logAndToast(t)
                }
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                try {
                    if (isChecked) {
                        if (textView.text.isEmpty())
                            timePickerDialog.show()
                        else
                            textView.visibility = View.VISIBLE
                    } else textView.visibility = View.INVISIBLE
                } catch (t: Throwable) {
                    logAndToast(t)
                }
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    protected fun logAndToast(t: Throwable) = logAndToast(t, this::class.java.toString())

    protected fun logAndToast(t: Throwable, tag: String?) {
        try {
            Log.e(tag, "", t)
            Toast.makeText(requireContext().applicationContext, t.toString(), Toast.LENGTH_LONG)
                .show()
        } catch (_: Throwable) {
        }
    }
}