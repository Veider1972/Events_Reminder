package ru.veider.eventsreminder.repo.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventSourceType
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.domain.PeriodType
import ru.veider.eventsreminder.presentation.ui.toInt
import ru.veider.eventsreminder.presentation.ui.toLocalDate
import ru.veider.eventsreminder.presentation.ui.toLocalTime
import java.security.spec.InvalidParameterSpecException
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "local_events")
data class LocalEvent(
    val type: EventType,
    val period: PeriodType?,
    val birthday: LocalDate?,
    val date: LocalDate,
    val time: LocalTime?,
    val timeNotifications: LocalTime?,
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0
    fun toEventData() = EventData(
        type,
        period,
        birthday,
        date,
        time,
        timeNotifications,
        name,
        _id,
        EventSourceType.LOCAL
    )
    companion object {
        @Throws(InvalidParameterSpecException::class)
        fun fromEventData(eventData: EventData) =
            if(eventData.sourceType == EventSourceType.LOCAL) LocalEvent(
            eventData.type,
            eventData.period,
            eventData.birthday,
            eventData.date,
            eventData.time,
            eventData.timeNotifications,
            eventData.name
        ).apply { _id = eventData.sourceId }
        else throw InvalidParameterSpecException("EventData.sourceType must be LOCAL")
    }
}
@Keep
class EventTypeConverter {
    @TypeConverter
    fun eventTypeToString(eventType: EventType) = eventType.toString()
    @TypeConverter
    fun stringToEventType(string: String) = EventType.valueOf(string)
}
@Keep
class PeriodTypeConverter {
    @TypeConverter
    fun periodTypeToString(period: PeriodType?) = period?.toString()
    @TypeConverter
    fun stringToPeriodType(string: String?) = string?.let { PeriodType.fromString(it) }
}
@Keep
class LocalDateConverter {
    @TypeConverter
    fun localDateToInt(date: LocalDate?) = date?.toInt()
    @TypeConverter
    fun intToLocalDate(int: Int?) = int?.toLocalDate()
}
@Keep
class LocalTimeConverter {
    @TypeConverter
    fun localTimeToInt(time: LocalTime?) = time?.toInt()
    @TypeConverter
    fun intToLocalTime(int: Int?) = int?.toLocalTime()
}