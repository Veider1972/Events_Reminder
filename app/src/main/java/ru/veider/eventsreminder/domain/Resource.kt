package ru.veider.eventsreminder.domain

sealed class ResourceState<T> {
    data class SuccessState<T>(val data: T) : ResourceState<T>()
    class ErrorState<T>(val error: Throwable) : ResourceState<T>()
}
