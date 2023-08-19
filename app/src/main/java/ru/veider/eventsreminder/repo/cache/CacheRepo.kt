package ru.veider.eventsreminder.repo.cache

import ru.veider.eventsreminder.domain.EventData

interface CacheRepo {
    fun getList():List<EventData>
    fun renew(events: List<EventData>)
}

