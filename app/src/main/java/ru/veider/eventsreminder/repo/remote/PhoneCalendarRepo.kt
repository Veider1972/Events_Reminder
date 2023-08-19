package ru.veider.eventsreminder.repo.remote

import ru.veider.eventsreminder.domain.EventData

interface PhoneCalendarRepo {
    fun loadEventCalendar(endDay:Int): List<EventData>
}