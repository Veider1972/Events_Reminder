package ru.veider.eventsreminder.domain

sealed class AppState{
    data class SuccessState<T>(val data: T) : AppState()
    object LoadingState : AppState()
    data class ErrorState(val error: Throwable) : AppState()
}
