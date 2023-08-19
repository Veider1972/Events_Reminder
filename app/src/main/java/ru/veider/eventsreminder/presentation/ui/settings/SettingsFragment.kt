package ru.veider.eventsreminder.presentation.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.google.gson.Gson
import com.rarepebble.colorpicker.ColorPreference
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.di.ViewModelFactory
import ru.veider.eventsreminder.domain.ResourceState
import ru.veider.eventsreminder.domain.SettingsData
import ru.veider.eventsreminder.presentation.MainActivity
import ru.veider.eventsreminder.presentation.ui.FontSizeSeekBarPreference
import ru.veider.eventsreminder.presentation.ui.myevents.MyEventsViewModel
import ru.veider.eventsreminder.presentation.ui.toInt
import ru.veider.eventsreminder.presentation.ui.toLocalTime
import ru.veider.eventsreminder.repo.Repo
import ru.veider.eventsreminder.usecases.toEventDto
import java.time.LocalTime
import javax.inject.Inject
import kotlin.coroutines.coroutineContext


class SettingsFragment : PreferenceFragmentCompat(), HasAndroidInjector {
	@Inject
	lateinit var settingsData: SettingsData

	@Inject
	lateinit var repo: Repo
	private val prefs by lazy {
		PreferenceManager.getDefaultSharedPreferences(requireActivity().applicationContext)
	}
	lateinit var androidInjector: DispatchingAndroidInjector<Any>
	override fun onAttach(context: Context) {
		try {
			AndroidSupportInjection.inject(this as Fragment)
			super.onAttach(context)
			prefs.registerOnSharedPreferenceChangeListener(bindPreferenceSummaryToValueListener)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	override fun onDisplayPreferenceDialog(preference: Preference) {
		try {
			if (preference is ColorPreference) {
				preference.showDialog(this, 0)
			} else super.onDisplayPreferenceDialog(preference)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	override fun androidInjector(): AndroidInjector<Any> {
		return androidInjector
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, key: String?) {
		try {
			setPreferencesFromResource(R.xml.preferences, key)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private val bindPreferenceSummaryToValueListener =
		SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
			try {
				(requireActivity() as MainActivity).setPreferences(preferences, key)
				findPreference<WidgetPreviewPreference>(getString(R.string.key_widget_appearance_by_default))?.renew()
			} catch (t: Throwable) {
				outputError(t)
			}
		}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		try {
			initPreferences()
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	override fun onDetach() {
		super.onDetach()
		try {
			val prefs =
				PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
			prefs.unregisterOnSharedPreferenceChangeListener(bindPreferenceSummaryToValueListener)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun initPreferences() {
		try {
			initDataSourcesPrefs()
			initNotificationPrefs()
			initWidgetDataPrefs()
			initWidgetPreview()
			initWidgetLookFontPrefs()
			initWidgetLookBackgroundPrefs()
			initSettingsExportImportPrefs()
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun initSettingsExportImportPrefs() {
		try {
			val exportSettingsButton: Preference? =
				findPreference(getString(R.string.key_export_settings_preference))
			exportSettingsButton?.setOnPreferenceClickListener {
				savePrefs.launch("EventsReminder.settings")
				true
			}
			val importSettingsButton: Preference? =
				findPreference(getString(R.string.key_import_settings_preference))
			importSettingsButton?.setOnPreferenceClickListener {
				loadPrefs.launch(arrayOf("*/*"))
				true
			}
			val exportEventsButton: Preference? =
				findPreference(getString(R.string.key_events_export))
			exportEventsButton?.setOnPreferenceClickListener {
				saveEvents.launch("EventsReminder.data")
				true
			}
			val importEventsButton: Preference? =
				findPreference(getString(R.string.key_events_import))
			importEventsButton?.setOnPreferenceClickListener {
				loadEvents.launch(arrayOf("*/*"))
				true
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private val savePrefs = registerForActivityResult(
		ActivityResultContracts.CreateDocument("*/*")
	) { uri ->
		try {
			uri?.let {
				if (ExportSettings.saveSharedPreferencesToFile(
						it,
						requireContext(),
						prefs
					)
				) Toast.makeText(context, getString(R.string.toast_msg_current_settings_saved), Toast.LENGTH_SHORT).show()
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private val loadPrefs = registerForActivityResult(
		ActivityResultContracts.OpenDocument()
	) { uri ->
		try {
			uri?.let {
				if (ExportSettings.loadSharedPreferencesFromFile(
						it,
						requireContext(),
						prefs
					)
				) findNavController().navigate(R.id.action_settings_self).also {
					Toast.makeText(context, getString(R.string.toast_msg_settings_load_successfully), Toast.LENGTH_SHORT).show()
				}
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private val saveEvents = registerForActivityResult(
		ActivityResultContracts.CreateDocument("*/*")
	) { uri ->
		try {
			uri?.let {
				CoroutineScope(Dispatchers.IO).launch {
					val data = repo.loadLocalData()

					if (data is ResourceState.SuccessState) {
						if (ExportEvents.saveEventsToFile(
								it,
								requireContext(),
								data.data.map { it.toEventDto() }
							)
						)
							withContext(Dispatchers.Main){
								Toast.makeText(context, getString(R.string.toast_msg_events_saved), Toast.LENGTH_SHORT).show()
							}

					}
				}
			}
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private val loadEvents = registerForActivityResult(
		ActivityResultContracts.OpenDocument()
	) { uri ->
		try {
			uri?.let {

				if (ExportEvents.loadEventsFromFile(
					it,
					requireContext(),
					repo
				))
					Toast.makeText(context, getString(R.string.toast_msg_events_loaded), Toast.LENGTH_SHORT).show()
			}
		} catch (e: Throwable) {
			outputError(e)
		}
	}

	private fun initWidgetLookBackgroundPrefs() { // Настройки фона
		try {
			initColorPreference(R.string.key_background_color_preference, settingsData.widgetBackgroundColor)
			initColorPreference(R.string.key_first_line_color_preference, settingsData.widgetLine1Color)
			initColorPreference(R.string.key_second_line_color_preference, settingsData.widgetLine2Color)
			initColorPreference(R.string.key_widget_border_color, settingsData.widgetBorderColor)
			initSeekBarPreference(R.string.key_widget_border_width, settingsData.widgetBorderWidth)
			initSeekBarPreference(R.string.key_widget_border_corners, settingsData.widgetBorderCornerRadius)
		} catch (t: Throwable) {
			outputError(t)
		}
	}


	private fun initWidgetLookFontPrefs() { // Настройки шрифта
		try {
			val chooseWidgetFontSize: FontSizeSeekBarPreference? =
				findPreference(getString(R.string.key_widget_font_size_preference))
			chooseWidgetFontSize?.value = prefs.getInt(
				getString(R.string.key_widget_font_size_preference),
				settingsData.widgetFontSize
			)
			chooseWidgetFontSize?.setOnPreferenceClickListener {
				Toast.makeText(
					context,
					getString(R.string.widget_fontsize_toast) + chooseWidgetFontSize.value.toString(),
					Toast.LENGTH_SHORT
				).show()
				true
			}
			initColorPreference(R.string.key_widget_birthday_font_color_preference, settingsData.widgetFontColorBirthday)
			initColorPreference(R.string.key_widget_holiday_font_color_preference, settingsData.widgetFontColorHoliday)
			initColorPreference(R.string.key_widget_simple_event_font_color_preference, settingsData.widgetFontColorSimpleEvent)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun initWidgetDataPrefs() { // Отображение данных
		try {
			initCheckBoxPreference(R.string.key_event_date_checkbox_preference, settingsData.showDateEvent)
			initCheckBoxPreference(R.string.key_event_time_checkbox_preference, settingsData.showTimeEvent)
			initCheckBoxPreference(R.string.key_age_checkbox_preference, settingsData.showAge)
			initSeekBarPreference(R.string.key_widget_interval_of_events_preference, settingsData.daysForShowEventsWidget)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun initWidgetPreview() {
		try {
			findPreference<WidgetPreviewPreference>(getString(R.string.key_widget_appearance_by_default))?.applySettings(
				settingsData
			)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun initNotificationPrefs() { // Настройка уведомлений
		try {
			val chooseNotificationStartTimeButton: Preference? =
				findPreference(getString(R.string.key_notification_start_time_preference))
			val time = prefs.getInt(
				getString(R.string.key_notification_start_time_preference),
				settingsData.timeToStartNotification
			).toLocalTime()

			chooseNotificationStartTimeButton?.let {
				it.summary = time.toString()
				it.setOnPreferenceClickListener {
					initTimePicker(
						requireContext(), time,
						chooseNotificationStartTimeButton
					)
					true
				}
			}
			initSeekBarPreference(R.string.key_minutes_before_notification_preference, settingsData.minutesForStartNotification)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun initDataSourcesPrefs() { // Источники данных
		try {
			initCheckBoxPreference(R.string.key_phonebook_datasource_checkbox_preference, settingsData.isDataContact)
			initCheckBoxPreference(R.string.key_calendar_datasource_checkbox_preference, settingsData.isDataCalendar)
			initSeekBarPreference(R.string.key_show_events_interval_preference, settingsData.daysForShowEvents)
		} catch (t: Throwable) {
			outputError(t)
		}
	}

	private fun initSeekBarPreference(itemId: Int, value: Int) {
		findPreference<SeekBarPreference>(getString(itemId))?.let {
			it.value =
				prefs.getInt(
					getString(itemId),
					value
				)
			it.seekBarIncrement = 1
		}
	}

	private fun initCheckBoxPreference(itemId: Int, value: Boolean) {
		findPreference<CheckBoxPreference>(getString(itemId))?.isChecked =
			prefs.getBoolean(getString(itemId), value)
	}

	private fun initColorPreference(itemId: Int, value: Int) {
		(findPreference(getString(itemId)) as ColorPreference?)
			?.setDefaultValue(value)
	}

	private fun outputError(t: Throwable) {
		try {
			Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show()
			Log.e(this::class.java.toString(), "", t)
		} catch (_: Throwable) {
		}
	}

	private fun initTimePicker(context: Context, curTime: LocalTime, preference: Preference) {
		try {
			val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
				try {
					val time = LocalTime.of(hour, minute)
					preference.summary = time.toString()
					prefs.edit().putInt(
						getString(R.string.key_notification_start_time_preference),
						time.toInt()
					).apply()
				} catch (t: Throwable) {
					logAndToast(t)
				}
			}
			val timePickerDialog = TimePickerDialog(
				context, R.style.date_picker, timeSetListener, curTime.hour,
				curTime.minute, true
			)
			timePickerDialog.show()

		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	private fun logAndToast(t: Throwable) = logAndToast(t, this::class.java.toString())

	private fun logAndToast(t: Throwable, tag: String?) {
		try {
			Log.e(tag, "", t)
			Toast.makeText(requireContext().applicationContext, t.toString(), Toast.LENGTH_LONG)
				.show()
		} catch (_: Throwable) {
		}
	}
}