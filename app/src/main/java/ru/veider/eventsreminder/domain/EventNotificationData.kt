package ru.veider.eventsreminder.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime
@Parcelize
data class EventNotificationData(
    var idNotification: Int?,
    var idAlarm: Int?,
    val type: EventType,
    val period: PeriodType?,
    val birthday: LocalDate?,
    val date: LocalDate,
    val time: LocalTime?,
    val timeNotifications: LocalTime?,
    val name: String,
    val sourceId: Long,
    val sourceType: EventSourceType
) : Parcelable
