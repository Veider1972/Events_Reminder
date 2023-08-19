package ru.veider.eventsreminder.presentation.ui.myevents

import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.veider.eventsreminder.App
import ru.veider.eventsreminder.domain.AppState
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.ResourceState
import ru.veider.eventsreminder.domain.SettingsData
import ru.veider.eventsreminder.presentation.ui.MAX_YEAR
import ru.veider.eventsreminder.presentation.ui.toInt
import ru.veider.eventsreminder.presentation.ui.toLocalDate
import ru.veider.eventsreminder.repo.Repo
import ru.veider.eventsreminder.repo.cache.CacheRepo
import ru.veider.eventsreminder.service.NotificationService
import ru.veider.eventsreminder.usecases.EVENTS_DATA
import ru.veider.eventsreminder.usecases.MINUTES_FOR_START_NOTIFICATION
import ru.veider.eventsreminder.usecases.TIME_TO_START_NOTIFICATION
import ru.veider.eventsreminder.widget.AppWidget
import java.lang.Math.min
import java.time.LocalDate
import javax.inject.Inject

class MyEventsViewModel @Inject constructor(
    private val repo: Repo,
    private val cache: CacheRepo,
    private val app : App,
    private val settingsData: SettingsData
) : ViewModel(), LifecycleObserver {
    val statesLiveData: MutableLiveData<AppState> = MutableLiveData()
    val cachedLocalEvents = mutableListOf<EventData>()
    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.IO
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable -> handleError(throwable) }
    )
    var localEventsJob: Job? = null

    /**
     * Хранимые события для работы diffUtils
     * */
    val storedEvents = mutableListOf<EventData>()
    fun handleError(error: Throwable) {
        try {
            statesLiveData.postValue(AppState.ErrorState(error))
        } catch (_: Throwable) {
        }
    }

    override fun onCleared() {
        try {
            super.onCleared()
            viewModelCoroutineScope.cancel()
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    fun loadMyEvents() {
        try {
            if (cachedLocalEvents.any())
                statesLiveData.value = AppState.SuccessState(cachedLocalEvents.toList())
            else statesLiveData.value = AppState.LoadingState
            localEventsJob?.let { return }
            localEventsJob = viewModelCoroutineScope.launch {
                when (val result = repo.loadLocalData()) {
                    is ResourceState.SuccessState -> {
                        val dataWithBrededEvents = mutableListOf<EventData>()
                        val startDate =
                            min(
                                result.data.firstOrNull()?.date?.toInt() ?: (MAX_YEAR * 10000),
                                LocalDate.now().toInt()
                            )
                        result.data.forEach { event ->
                            /*if (event.period != null)
                                dataWithBrededEvents.addAll(
                                    event.breedPeriodicEvents(
                                        startDate.toLocalDate(),
                                        LocalDate.now()
                                            .plusDays(365L)
                                    )
                                )
                            else */dataWithBrededEvents.add(event)
                        }
                        addToCachedLocalEvents(dataWithBrededEvents)
                        renewCache()
                        statesLiveData.postValue(AppState.SuccessState(cachedLocalEvents.toList()))
                    }

                    is ResourceState.ErrorState -> handleError(result.error)
                }
            }
            localEventsJob?.invokeOnCompletion {
                localEventsJob = null
            }
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    private fun addToCachedLocalEvents(events: List<EventData>) {
        try {
            cachedLocalEvents.clear()
            arrangeByDatesToSortedMap(events).forEach {
                it.value.sortBy(EventData::name)
                it.value.sortBy(EventData::time)
                it.value.sortBy(EventData::timeNotifications)
                cachedLocalEvents.addAll(it.value)
            }
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    private fun arrangeByDatesToSortedMap(events: List<EventData>): Map<LocalDate, MutableList<EventData>> {
        val mapToSort = mutableMapOf<LocalDate, MutableList<EventData>>()
        try {
            events.forEach { event ->
                mapToSort.getOrPut(event.date) { mutableListOf() }.add(event)
            }
        } catch (t: Throwable) {
            handleError(t)
        }
        return mapToSort.toSortedMap()
    }

    fun deleteMyEvent(event: EventData) {
        try {
            localEventsJob?.let { return }
            localEventsJob = viewModelCoroutineScope.launch {
                repo.deleteLocalEvent(event)
                cachedLocalEvents.removeIf { it.sourceId == event.sourceId }
                statesLiveData.postValue(AppState.SuccessState(cachedLocalEvents.toList()))
                renewCache()
            }
            localEventsJob?.invokeOnCompletion {
                localEventsJob = null
            }
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    private suspend fun renewCache() {
        val daysToPutInCache =
            Integer.max(
                settingsData.daysForShowEvents,
                settingsData.daysForShowEventsWidget
            )
        val result = repo.loadData(
            daysToPutInCache,
            settingsData.isDataContact, settingsData.isDataCalendar
        )
        if (result is ResourceState.SuccessState) {
            cache.renew(
                applyFilterToAllEventsFromRepo(daysToPutInCache, result.data)
            )
            updateNotifications(result.data)
            updateWidget()
        }
    }

    private fun updateNotifications(events:List<EventData>) {
        app.startService(
            Intent(app, NotificationService::class.java).apply {
                putExtra(
                    MINUTES_FOR_START_NOTIFICATION,
                    settingsData.minutesForStartNotification,
                )
                putExtra(
                    TIME_TO_START_NOTIFICATION,
                    settingsData.timeToStartNotification,
                )
                putParcelableArrayListExtra(EVENTS_DATA, ArrayList(events))
            },
        )
    }

    private fun updateWidget() {
        try {
            Dispatchers.Main.run {
                AppWidget.sendRefreshBroadcast(app)
            }
        }  catch (t: Throwable) {
            handleError(t)
        }
    }

    private fun applyFilterToAllEventsFromRepo(
        daysToPutInCache: Int,
        allEventsFromRepo: List<EventData>
    ): List<EventData> {
        val retList = mutableListOf<EventData>()
        try {
            val mapToSort = mutableMapOf<LocalDate, MutableList<EventData>>()
            val startDate = min(
                allEventsFromRepo.firstOrNull()?.date?.toInt() ?: (MAX_YEAR * 10000),
                LocalDate.now().toInt()
            )
            val endDate = LocalDate.now().plusDays(daysToPutInCache.toLong())
            allEventsFromRepo.forEach { event ->
                processOrBreedEvent(event, startDate.toLocalDate(), endDate, mapToSort)
            }

            mapToSort.toSortedMap().forEach {
                it.value.sortBy(EventData::name)
                it.value.sortBy(EventData::time)
                it.value.sortBy(EventData::timeNotifications)
                retList.addAll(it.value)
            }
        } catch (t: Throwable) {
            handleError(t)
        }
        return retList
    }

    private fun processOrBreedEvent(
        event: EventData,
        startDate: LocalDate,
        endDate: LocalDate,
        mapToSort: MutableMap<LocalDate, MutableList<EventData>>
    ) {
        try {
            if (event.period != null)
                event.breedPeriodicEvents(startDate, endDate).forEach {
                    if (event.date.isAfter(startDate)
                        && event.date.isBefore(endDate)
                        || event.date.isEqual(startDate)
                    )
                        mapToSort.getOrPut(it.date) { mutableListOf() }.add(it)
                }
            else
                mapToSort.getOrPut(event.date) { mutableListOf() }.add(event)
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    fun clearAllLocalEvents() {
        try {
            localEventsJob?.let { return }
            localEventsJob = viewModelCoroutineScope.launch {
                repo.clearAllLocalEvents()
                cachedLocalEvents.clear()
                statesLiveData.postValue(AppState.SuccessState(cachedLocalEvents.toList()))
                renewCache()
            }
            localEventsJob?.invokeOnCompletion {
                localEventsJob = null
            }
        } catch (t: Throwable) {
            handleError(t)
        }
    }
}