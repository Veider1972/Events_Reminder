package ru.veider.eventsreminder.presentation.ui.myevents

import ru.veider.eventsreminder.domain.EventData

interface EventEditor {
    fun openEditEvent(event: EventData)
    fun openRemoveEvent(event: EventData)
}