package ru.veider.eventsreminder.usecases

import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.ResourceState
import ru.veider.eventsreminder.domain.SettingsData

interface UseCases {
    suspend fun loadListEvent(settings: SettingsData): ResourceState<List<EventData>>
}