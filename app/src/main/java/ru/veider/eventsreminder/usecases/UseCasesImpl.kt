package ru.veider.eventsreminder.usecases

import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.ResourceState
import ru.veider.eventsreminder.domain.SettingsData
import ru.veider.eventsreminder.repo.Repo

class UseCasesImpl (val repo:Repo):UseCases{
    override suspend fun loadListEvent(settings: SettingsData):ResourceState<List<EventData>>{
        return when(val loadResponse = repo.loadData(settings.daysForShowEvents,settings.isDataContact,settings.isDataCalendar)){
            is ResourceState.SuccessState -> {
                ResourceState.SuccessState(getActualListEvents(loadResponse.data,settings))
            }

            is ResourceState.ErrorState -> loadResponse
        }
    }
}