package ru.veider.eventsreminder.repo.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocalEventsDAO {
	@Query("SELECT * FROM local_events ORDER BY date ASC")
	fun getLocalEvents(): List<LocalEvent>

	@Query("SELECT * FROM local_events WHERE _id=:id")
	fun getLocalEvent(id: Long): LocalEvent

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(localEvent: LocalEvent)

	@Update
	fun update(localEvent: LocalEvent)

	@Delete
	fun delete(localEvent: LocalEvent)

	@Query("DELETE FROM local_events")
	fun clear()
}