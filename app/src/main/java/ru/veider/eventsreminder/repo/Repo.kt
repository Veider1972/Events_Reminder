package ru.veider.eventsreminder.repo

import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.ResourceState

interface Repo {
    suspend fun loadData(daysForShowEvents:Int,isDataContact:Boolean,isDataCalendar:Boolean): ResourceState<List<EventData>>
    suspend fun loadLocalData(): ResourceState<List<EventData>>
    suspend fun deleteLocalEvent(eventData: EventData)

    suspend fun addLocalEvent(eventData: EventData)
    suspend fun clearAllLocalEvents()
}