package ru.veider.eventsreminder.di


import dagger.Module
import dagger.Provides
import ru.veider.eventsreminder.domain.SettingsData
import javax.inject.Singleton

@Module
class SettingsModule {
    @Singleton
    @Provides
    fun provideSettings() = SettingsData()
}