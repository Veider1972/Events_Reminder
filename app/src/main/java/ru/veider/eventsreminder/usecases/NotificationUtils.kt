package ru.veider.eventsreminder.usecases

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.presentation.MainActivity
import java.time.LocalDateTime
import java.time.ZoneId

const val EVENTS_DATA = "EVENT"
const val MINUTES_FOR_START_NOTIFICATION = "MinutesForStartNotification"
const val TIME_TO_START_NOTIFICATION = "TimeToStartNotification"
const val NOTIFY_ABOUT_EVENT = "NotifyAboutEvent"

object NotificationUtils {
	const val SIMPLE_CHANNEL_ID = "SimpleEventsReminder"
	const val ALARM_CHANNEL_ID = "AlarmEventsReminder"

	fun createNotificationChannel(context: Context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val simpleChannel = NotificationChannel(
				SIMPLE_CHANNEL_ID,
				context.getString(R.string.notifications_simple_channel_name),
				NotificationManager.IMPORTANCE_NONE
			).apply {
				description = context.getString(R.string.notifications_simple_channel_description)
			}
			val alarmChannel = NotificationChannel(
				ALARM_CHANNEL_ID,
				context.getString(R.string.notifications_alarm_channel_name),
				NotificationManager.IMPORTANCE_HIGH
			).apply {
				description = context.getString(R.string.notifications_simple_channel_description)
			}

			(context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
				createNotificationChannel(simpleChannel)
				createNotificationChannel(alarmChannel)
			}
		}
	}

	fun sendSimpleNotification(context:Context, idNotification: Int, title: String, text: String){
		val resultPendingIntent = PendingIntent.getActivity(
			context, 0, Intent(context, MainActivity::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT
		)
		val builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notifications_24dp)
			.setContentTitle(title)
			.setStyle(NotificationCompat.BigTextStyle().bigText(text))
			.setContentText(text)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setContentIntent(resultPendingIntent)
			.setAutoCancel(false)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

		if (ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.POST_NOTIFICATIONS
			) != PackageManager.PERMISSION_GRANTED
		) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return
		}
		NotificationManagerCompat.from(context).notify(idNotification, builder.build())
	}

	fun sendAlarmNotification(context: Context, idNotification: Int, title: String, text: String,dateTime: LocalDateTime?) {
		val resultPendingIntent = PendingIntent.getActivity(
			context, 0, Intent(context, MainActivity::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT
		)
		val zdt = dateTime?.atZone(ZoneId.systemDefault())
		val builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notifications_24dp)
			.setContentTitle(title)
			.setStyle(NotificationCompat.BigTextStyle().bigText(text))
			.setContentText(text)
			.setWhen(zdt?.toInstant()?.toEpochMilli()?:System.currentTimeMillis())
			.setShowWhen(zdt != null)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setContentIntent(resultPendingIntent)
			.setAutoCancel(true)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

		if (ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.POST_NOTIFICATIONS
			) != PackageManager.PERMISSION_GRANTED
		) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return
		}
		NotificationManagerCompat.from(context).notify(idNotification, builder.build())
	}
}