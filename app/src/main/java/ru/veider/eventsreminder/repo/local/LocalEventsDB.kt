package ru.veider.eventsreminder.repo.local

import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Keep
@Database(exportSchema = false, entities = [LocalEvent::class], version = 1)
@TypeConverters(
	LocalDateConverter::class, LocalTimeConverter::class,
	EventTypeConverter::class, PeriodTypeConverter::class
)
abstract class LocalEventsDB : RoomDatabase() {
	abstract fun getLocalEventsDao(): LocalEventsDAO
}
