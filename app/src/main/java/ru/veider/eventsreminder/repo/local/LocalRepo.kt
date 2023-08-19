package ru.veider.eventsreminder.repo.local

import ru.veider.eventsreminder.domain.EventData

interface LocalRepo {
    fun addEvent(event:EventData)
//    fun updateEvent(event: EventData)
//    fun getEvent(sourceId : Long) : EventData
    fun deleteEvent(event: EventData)
    fun clear()
    fun getList():List<EventData>
}