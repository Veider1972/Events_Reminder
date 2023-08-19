package ru.veider.eventsreminder.presentation.ui.dashboard

import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ru.veider.eventsreminder.App
import ru.veider.eventsreminder.domain.AppState
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.domain.ResourceState
import ru.veider.eventsreminder.domain.SettingsData
import ru.veider.eventsreminder.presentation.ui.MAX_YEAR
import ru.veider.eventsreminder.presentation.ui.safeWithYear
import ru.veider.eventsreminder.presentation.ui.toInt
import ru.veider.eventsreminder.presentation.ui.toLocalDate
import ru.veider.eventsreminder.repo.Repo
import ru.veider.eventsreminder.repo.cache.CacheRepo
import ru.veider.eventsreminder.service.NotificationService
import ru.veider.eventsreminder.usecases.EVENTS_DATA
import ru.veider.eventsreminder.usecases.MINUTES_FOR_START_NOTIFICATION
import ru.veider.eventsreminder.usecases.TIME_TO_START_NOTIFICATION
import ru.veider.eventsreminder.widget.AppWidget
import java.lang.Integer.max
import java.time.LocalDate
import javax.inject.Inject


class DashboardViewModel @Inject constructor(
    private val settingsData: SettingsData,
    private val repo: Repo,
    private val cacheRepo: CacheRepo,
    private val app: App
) : ViewModel(), LifecycleObserver {
    val statesLiveData: MutableLiveData<AppState> = MutableLiveData()
    private var allEventsFromRepo = listOf<EventData>()
    private val cachedEventsList = mutableListOf<EventData>()
    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.IO
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable -> handleError(throwable) }
    )
    private var eventsListJob: Job? = null

    /**
     * Используется для хранения списка из DashboardRecyclerViewAdapter
     * */
    val storedFilteredEvents = mutableListOf<EventData>()
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

    private fun List<EventData>.filterToDashboard() =
        try {
            takeWhile { eventData ->
                eventData.date <= LocalDate.now()
                    .plusDays(settingsData.daysForShowEvents.toLong())
            }
        } catch (t: Throwable) {
            handleError(t)
            listOf()
        }

    fun loadEvents() {
        try {
            if (cachedEventsList.any())
                statesLiveData.value = AppState.SuccessState(cachedEventsList.filterToDashboard())
            else statesLiveData.value = AppState.LoadingState
            eventsListJob?.let { return }
            eventsListJob = viewModelCoroutineScope.launch {
                val daysToPutInCache =
                    max(settingsData.daysForShowEvents, settingsData.daysForShowEventsWidget)
                if (cachedEventsList.isEmpty()) {
                    val cached = cacheRepo.getList()
                    if (cached.any())
                        statesLiveData.postValue(AppState.SuccessState(cached.filterToDashboard()))
                }

                val result = repo.loadData(
                    daysToPutInCache,
                    settingsData.isDataContact, settingsData.isDataCalendar
                )
                when (result) {
                    is ResourceState.SuccessState -> {
                        val dataWithBrededEvents = mutableListOf<EventData>()
                        val startDate =
                            Math.min(
                                result.data.firstOrNull()?.date?.toInt() ?: (MAX_YEAR * 10000),
                                LocalDate.now().toInt()
                            )
                        result.data.forEach { event ->
                            if (event.period != null)
                                dataWithBrededEvents.addAll(
                                    event.breedPeriodicEvents(
                                        startDate.toLocalDate(),
                                        LocalDate.now()
                                            .plusDays(settingsData.daysForShowEvents.toLong())
                                    )
                                )
                            else dataWithBrededEvents.add(event)
                        }
                        allEventsFromRepo = dataWithBrededEvents
                        applyFilterToAllEventsFromRepo(daysToPutInCache)
                        cacheRepo.renew(cachedEventsList)
                        updateNotifications(result.data)
                        updateWidget()
                        statesLiveData.postValue(
                            AppState.SuccessState(
                                cachedEventsList.filterToDashboard()
                            )
                        )
                    }

                    is ResourceState.ErrorState -> handleError(result.error)
                }
            }
            eventsListJob?.invokeOnCompletion {
                eventsListJob = null
            }
        } catch (t: Throwable) {
            handleError(t)
        }
    }
    private fun updateNotifications(events: List<EventData>) {
        try{
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
        )}
        catch (t: Throwable) {
            handleError(t)
        }
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

    fun addLocalEvent(eventData: EventData) {
        try {
            do {
                eventsListJob?.let { Thread.sleep(1) }
            } while (eventsListJob != null)
            eventsListJob = viewModelCoroutineScope.launch {
                repo.addLocalEvent(eventData)
            }
            eventsListJob?.invokeOnCompletion {
                eventsListJob = null
                loadEvents()
            }
        } catch (t: Throwable) {
            handleError(t)
        }
    }

    fun getDaysToShowEventsCount() = settingsData.daysForShowEvents
    private fun applyFilterToAllEventsFromRepo(daysToPutInCache: Int) {
        try {
            cachedEventsList.clear()
            val mapToSort = mutableMapOf<LocalDate, MutableList<EventData>>()
            val startDate = LocalDate.now()
            val endDate = startDate.plusDays(daysToPutInCache.toLong())
            allEventsFromRepo.forEach { event ->
                    processEvent(event, startDate, endDate, mapToSort)
            }
            mapToSort.toSortedMap().forEach {
                it.value.sortBy(EventData::name)
                it.value.sortBy(EventData::time)
                it.value.sortBy(EventData::timeNotifications)
                cachedEventsList.addAll(it.value)
            }
        } catch (t: Throwable) {
            handleError(t)
        }
    }
    private fun processEvent(
        event: EventData,
        startDate: LocalDate,
        endDate: LocalDate,
        mapToSort: MutableMap<LocalDate, MutableList<EventData>>
    ) {
        try {
            if (event.date.isAfter(startDate)
                && event.date.isBefore(endDate)
                || event.date.isEqual(startDate)
            )
                mapToSort.getOrPut(event.date) { mutableListOf() }.add(event)
        } catch (t: Throwable) {
            handleError(t)
        }
    }
}