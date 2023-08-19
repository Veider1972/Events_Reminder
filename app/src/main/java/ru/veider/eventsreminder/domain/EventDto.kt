package ru.veider.eventsreminder.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class EventDto(
	val type: Int,
	val period: Int?,
	val birthday: Long?,
	val date: Long,
	val time: Int?,
	val timeNotifications: Int?,
	val name:String,
	val sourceId:Long,
	val sourceType: Int
) : Parcelable