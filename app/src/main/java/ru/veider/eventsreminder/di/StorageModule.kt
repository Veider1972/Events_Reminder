package ru.veider.eventsreminder.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.veider.eventsreminder.repo.local.LocalEventsDB
import javax.inject.Singleton

@Module
class StorageModule {

    @Singleton
    @Provides
    fun provideDatabase(context: Context): LocalEventsDB =
        Room.databaseBuilder(context, LocalEventsDB::class.java, "localevents.db")
            .build()
}