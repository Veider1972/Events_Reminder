package ru.veider.eventsreminder.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EventSourceType : Parcelable {
    CALENDAR,
    CONTACTS,
    LOCAL
}

fun Int.toEventSourceType() : EventSourceType =
    when(this){
        EventSourceType.CALENDAR.ordinal -> EventSourceType.CALENDAR
        EventSourceType.CONTACTS.ordinal -> EventSourceType.CONTACTS
        else -> EventSourceType.LOCAL
    }