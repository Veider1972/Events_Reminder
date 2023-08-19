package ru.veider.eventsreminder.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EventType : Parcelable {
    SIMPLE,
    BIRTHDAY,
    HOLIDAY;
    fun getString(): String =
        when (this) {
            SIMPLE -> "Событие"
            BIRTHDAY -> "День Рождения"
            HOLIDAY -> "Праздник"
        }
}

fun Int.toEventType() =
    when (this){
        EventType.BIRTHDAY.ordinal -> EventType.BIRTHDAY
        EventType.HOLIDAY.ordinal -> EventType.HOLIDAY
        else -> EventType.SIMPLE
    }