package ru.veider.eventsreminder.presentation

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import dagger.android.support.DaggerAppCompatActivity
import ru.veider.eventsreminder.App
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.databinding.ActivityMainBinding
import ru.veider.eventsreminder.domain.SettingsData
import ru.veider.eventsreminder.service.NotificationService
import ru.veider.eventsreminder.usecases.MINUTES_FOR_START_NOTIFICATION
import ru.veider.eventsreminder.usecases.TIME_TO_START_NOTIFICATION
import ru.veider.eventsreminder.widget.AppWidget
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private var doubleBackToExitPressedOnce = false
	private lateinit var navController: NavController

	@Inject
	lateinit var settings: SettingsData

	companion object {
		const val TAG = "MainActivity"
	}

	private lateinit var oneTapClient: SignInClient
	private lateinit var signInRequest: BeginSignInRequest
	private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
	private var showOneTapUI = true
	private lateinit var firebaseAuth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		firebaseAuth = FirebaseAuth.getInstance()
		oneTapClient = Identity.getSignInClient(this)
		signInRequest = BeginSignInRequest.builder()
			.setPasswordRequestOptions(
				BeginSignInRequest.PasswordRequestOptions.builder()
					.setSupported(true)
					.build()
			)
			.setGoogleIdTokenRequestOptions(
				BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
					.setSupported(true)
					// Your server's client ID, not your Android client ID.
					.setServerClientId(getString(R.string.client_id))
					// Only show accounts previously used to sign in.
					.setFilterByAuthorizedAccounts(true)
					.build()
			)
			// Automatically sign in when exactly one credential is retrieved.
			.setAutoSelectEnabled(true)
			.build()
		oneTapClient.beginSignIn(signInRequest)
			.addOnSuccessListener(this) { result ->
				try {
					startIntentSenderForResult(
						result.pendingIntent.intentSender, REQ_ONE_TAP,
						null, 0, 0, 0, null
					)
				} catch (e: IntentSender.SendIntentException) {
					Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
				}
			}
			.addOnFailureListener(this) { e ->
				// No saved credentials found. Launch the One Tap sign-up flow, or
				// do nothing and continue presenting the signed-out UI.
				Log.d(TAG, e.localizedMessage)
			}

		try {
			val isParamsSetRequired =
				!setPreferences(PreferenceManager.getDefaultSharedPreferences(applicationContext))
			binding = ActivityMainBinding.inflate(layoutInflater)
			supportActionBar?.setDisplayHomeAsUpEnabled(true)
			setContentView(binding.root)

			initNavController()
			if (isParamsSetRequired)
				navController.navigate(R.id.settingsFragment)
		} catch (t: Throwable) {
			logAndToast(t)
		}


	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			REQ_ONE_TAP -> {
				try {
					val credential = oneTapClient.getSignInCredentialFromIntent(data)
					val idToken = credential.googleIdToken
					val username = credential.id
					val password = credential.password
					when {
						idToken != null -> {
							// Got an ID token from Google. Use it to authenticate
							// with your backend.
							Log.d(TAG, "Got ID token.")
						}

						password != null -> {
							// Got a saved username and password. Use them to authenticate
							// with your backend.
							Log.d(TAG, "Got password.")
						}

						else -> {
							// Shouldn't happen.
							Log.d(TAG, "No ID token or password!")
						}
					}
				} catch (e: ApiException) {
					when (e.statusCode) {
						CommonStatusCodes.CANCELED -> {
							Log.d(TAG, "One-tap dialog was closed.")
							// Don't re-prompt the user.
							showOneTapUI = false
						}

						CommonStatusCodes.NETWORK_ERROR -> {
							Log.d(TAG, "One-tap encountered a network error.")
							// Try again or just ignore.
						}

						else -> {
							Log.d(
								TAG, "Couldn't get credential from result." +
										" (${e.localizedMessage})"
							)
						}
					}
				}
			}
		}
	}


	private fun initNavController() {
		try {
			navController = findNavController(R.id.nav_host_fragment_activity_main)
			val appBarConfiguration = AppBarConfiguration(
				setOf(
					R.id.dashboardFragment, R.id.myEventsFragment, R.id.settingsFragment,
					R.id.chooseNewEventTypeDialog, R.id.editBirthdayDialog,
					R.id.editHolidayDialog, R.id.editSimpleEventDialog
				)
			)
			setupActionBarWithNavController(navController, appBarConfiguration)
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.overflow_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean =
		try {
			NavigationUI.onNavDestinationSelected(item, navController)
			super.onOptionsItemSelected(item)
		} catch (t: Throwable) {
			logAndToast(t)
			false
		}

	@Deprecated("Deprecated in Java")
	override fun onBackPressed() {
		try {
			val navController = findNavController(R.id.nav_host_fragment_activity_main)
			if (doubleBackToExitPressedOnce || navController.backQueue.count() > 2) {
				super.onBackPressed()
				return
			}
			this.doubleBackToExitPressedOnce = true
			Toast.makeText(
				this,
				getString(R.string.toast_msg_double_back_pressure_btn),
				Toast.LENGTH_SHORT
			).show()

            Handler(Looper.getMainLooper()).postDelayed(
                {
                    try {
                        doubleBackToExitPressedOnce = false
                    } catch (_: Throwable) {
                    }
                },
                2000
            )
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

	private val calendarContactsPermission = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { map ->
		try {
			if (
				(!settings.isDataCalendar ||
						map[Manifest.permission.READ_CALENDAR] == true) &&
				(!settings.isDataContact ||
						map[Manifest.permission.READ_CONTACTS] == true) &&
				map[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
			)
				initReminderRights()
			else showAskWhyDialog()
		} catch (t: Throwable) {
			logAndToast(t)
		}
	}

    fun initReminderRights() {
        try {
            val rightsToDemand = mutableListOf<String>()
            rightsToDemand.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (settings.isDataCalendar)
                rightsToDemand.add(Manifest.permission.READ_CALENDAR)
            if (settings.isDataContact)
                rightsToDemand.add(Manifest.permission.READ_CONTACTS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager =
                    ContextCompat.getSystemService(applicationContext, AlarmManager::class.java)
                if (alarmManager?.canScheduleExactAlarms() == false) {
                    when {
                        Build.VERSION.SDK_INT >= 33 -> rightsToDemand.add(Manifest.permission.USE_EXACT_ALARM)
                        else -> rightsToDemand.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rightsToDemand.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            if (rightsToDemand.any() && !checkPermission()) {
                calendarContactsPermission.launch(
                    rightsToDemand.toTypedArray()
                )
            } else {
                Log.d(TAG, getString(R.string.log_msg_rights_check_succeeded))
                navController.navigate(R.id.dashboardFragment)
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    fun checkPermission(): Boolean {
        return try {
            (!settings.isDataCalendar ||
                    ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.READ_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED) && (
                    !settings.isDataContact ||
                            ContextCompat.checkSelfPermission(
                                applicationContext, Manifest.permission.READ_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED) &&
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED } else true
                    && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager =
                                ContextCompat.getSystemService(applicationContext, AlarmManager::class.java)
                            alarmManager?.canScheduleExactAlarms() == true
                                    && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            ContextCompat.checkSelfPermission(applicationContext,
                                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            else true
                        } else true
        } catch (t: Throwable) {
            logAndToast(t)
            false
        }
    }

    private fun showAskWhyDialog() {
        try {
            val builder = AlertDialog.Builder(this)
            val rightCalendarToDemand = if (settings.isDataCalendar) "календарю" else ""
            val rightContactToDemand = if (settings.isDataContact) "контактам" else ""
            val rightsToDemand = rightCalendarToDemand +
                    (if (settings.isDataCalendar && settings.isDataContact) ", " else "") +
                    rightContactToDemand + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ", уведомлениям, будильникам и напоминаниям"
                } else ", будильникам и напоминаниям"
            } else ""
            builder.setTitle(getString(R.string.demands_dialog_title_first))
                .setMessage(buildString {
                    append(getString(R.string.demands_dialog_title_second))
                    append(rightsToDemand)
                    append(getString(R.string.demands_dialog_title_third))
                })
                .setCancelable(false)
                .setPositiveButton("      права") { _, _ ->
                    try {
                        // открываем настройки приложения, чтобы пользователь дал разрешение вручную
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", this.packageName, null)
                        intent.data = uri
                        getPermissionManually.launch(intent)
                    } catch (t: Throwable) {
                        logAndToast(t)
                    }
                }
                .setNegativeButton("настройки     ") { _, _ ->
                    try {
                        navController.navigate(R.id.settingsFragment)
                    } catch (t: Throwable) {
                        logAndToast(t)
                    }
                }
            val dlg = builder.create()
            dlg.show()
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    private val getPermissionManually = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        try {
            initReminderRights()
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }


//    fun updateWidget() {
//        try {
//            runOnUiThread {
//                AppWidget.sendRefreshBroadcast(application as App)
//            }
//        } catch (t: Throwable) {
//            logAndToast(t)
//        }
//    }
//
//    fun updateNotificationService(eventsList: List<EventData>) {
//        try {
//            startService(
//                Intent(applicationContext, NotificationService::class.java).apply {
//                    putExtra(
//                        MINUTES_FOR_START_NOTIFICATION,
//                        settings.minutesForStartNotification,
//                    )
//                    putExtra(
//                        TIME_TO_START_NOTIFICATION,
//                        settings.timeToStartNotification,
//                    )
//                    putParcelableArrayListExtra(EVENTS_DATA, ArrayList(eventsList))
//                },
//            )
//        } catch (t: Throwable) {
//            logAndToast(t)
//        }
//    }

	/**
	 * Применить параметры из настроек
	 * @param preferences набор настроек для применения в приложении
	 * @param key ключ с названием конкретной настройки
	 * @return true в случае успешного применения всех параметров
	 *         false если требуется установка параметров
	 * (в случае [null] - будут применены все настройки)
	 * */
	fun setPreferences(preferences: SharedPreferences, key: String? = null): Boolean {
		var ret = false
		try {
			if (preferences.contains(getString(R.string.key_phonebook_datasource_checkbox_preference)))
				ret =
					true // Если хотя бы один параметр инициализирован, то взводим флаг применения параметров

			if (loadBooleanData(preferences, key, R.string.key_phonebook_datasource_checkbox_preference,
					{ settings.isDataContact = it }, settings.isDataContact, { }
				)
			) return ret

			if (loadBooleanData(preferences, key, R.string.key_calendar_datasource_checkbox_preference,
					{ settings.isDataCalendar = it }, settings.isDataCalendar, { }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_show_events_interval_preference,
					{ settings.daysForShowEvents = it }, settings.daysForShowEvents, { refresh() }
				)
			) return ret

			if (loadBooleanData(preferences, key, R.string.key_event_date_checkbox_preference,
					{ settings.showDateEvent = it }, settings.showDateEvent, { refresh() }
				)
			) return ret

			if (loadBooleanData(preferences, key, R.string.key_event_time_checkbox_preference,
					{ settings.showTimeEvent = it }, settings.showTimeEvent, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_font_size_preference,
					{ settings.widgetFontSize = it }, settings.widgetFontSize, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_birthday_font_color_preference,
					{ settings.widgetFontColorBirthday = it }, settings.widgetFontColorBirthday, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_holiday_font_color_preference,
					{ settings.widgetFontColorHoliday = it }, settings.widgetFontColorHoliday, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_background_color_preference,
					{ settings.widgetBackgroundColor = it }, settings.widgetBackgroundColor, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_border_color,
					{ settings.widgetBorderColor = it }, settings.widgetBorderColor, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_border_width,
					{ settings.widgetBorderWidth = it }, settings.widgetBorderWidth, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_border_corners,
					{ settings.widgetBorderCornerRadius = it }, settings.widgetBorderCornerRadius, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_simple_event_font_color_preference,
					{ settings.widgetFontColorSimpleEvent = it }, settings.widgetFontColorSimpleEvent, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_first_line_color_preference,
					{ settings.widgetLine1Color = it }, settings.widgetLine1Color, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_second_line_color_preference,
					{ settings.widgetLine2Color = it }, settings.widgetLine2Color, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_widget_interval_of_events_preference,
					{ settings.daysForShowEventsWidget = it }, settings.daysForShowEventsWidget, { refresh() }
				)
			) return ret

			if (loadBooleanData(preferences, key, R.string.key_age_checkbox_preference,
					{ settings.showAge = it }, settings.showAge, { refresh() }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_minutes_before_notification_preference,
					{ settings.minutesForStartNotification = it }, settings.minutesForStartNotification,
					{ notify(MINUTES_FOR_START_NOTIFICATION, settings.minutesForStartNotification) }
				)
			) return ret

			if (loadIntData(preferences, key, R.string.key_notification_start_time_preference,
					{ settings.timeToStartNotification = it }, settings.timeToStartNotification,
					{ notify(TIME_TO_START_NOTIFICATION, settings.timeToStartNotification) }
				)
			) return ret

		} catch (t: Throwable) {
			logAndToast(t)
			return false
		}
		return ret
	}

	private fun refresh() {
		runOnUiThread {
			AppWidget.sendRefreshBroadcast(application as App)
		}
	}

	private fun notify(key: String, value: Int) {
		startService(Intent(this, NotificationService::class.java).apply { putExtra(key, value) })
	}

	private fun loadIntData(
		preferences: SharedPreferences,
		key: String?,
		compareKey: Int,
		value: (Int) -> Unit,
		defaultValue: Int,
		runIfNotNull: () -> Unit
	): Boolean {
		if (key.isNullOrBlank() || key == getString(compareKey)) {
			value(
				preferences.getInt(
					getString(compareKey),
					defaultValue
				)
			)
			if (!key.isNullOrBlank()) {
				runIfNotNull()
				return true
			}
		}
		return false
	}

	private fun loadBooleanData(
		preferences: SharedPreferences,
		key: String?,
		compareKey: Int,
		value: (Boolean) -> Unit,
		defaultValue: Boolean,
		runIfNotNull: () -> Unit
	): Boolean {
		if (key.isNullOrBlank() || key == getString(compareKey)) {
			value(
				preferences.getBoolean(
					getString(compareKey),
					defaultValue
				)
			)
			if (!key.isNullOrBlank()) {
				runIfNotNull()
				return true
			}
		}
		return false
	}

    private fun logAndToast(t:Throwable) = logAndToast(t,this::class.java.toString())

    private fun logAndToast(t: Throwable, TAG:String) {
        try {
            Log.e(TAG, "", t)
            Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG).show()
        } catch (_: Throwable) {
        }
    }
}