package ru.veider.eventsreminder.repo.remote

import ru.veider.eventsreminder.domain.EventData

interface PhoneContactsRepo {
    fun loadBirthDayEvents(endDay: Int): List<EventData>
}