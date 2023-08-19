package ru.veider.eventsreminder.usecases

import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventNotificationData
import ru.veider.eventsreminder.domain.EventSourceType
import ru.veider.eventsreminder.domain.EventType
import ru.veider.eventsreminder.domain.PeriodType
import ru.veider.eventsreminder.presentation.ui.MAX_YEAR
import ru.veider.eventsreminder.presentation.ui.safeWithYear
import java.time.*

fun getCelebrationDateForBirthDay(birthDay: LocalDate) =
    with(LocalDate.now()) {
        if (birthDay.safeWithYear(year) < this)
            LocalDate.of(year + 1, birthDay.month, birthDay.dayOfMonth)
        else
            LocalDate.of(year, birthDay.month, birthDay.dayOfMonth)
    }

fun extractBirthday(text: String): LocalDate {
    //2017-05-23
    // "--05-27" - пример строки дня рождения без года
    val numbers = text.split('-', '/', '.').filter { s -> s != "" }
    return LocalDate.of(
        if (numbers.size == 3)
            numbers[0].toInt()
        else MAX_YEAR,
        numbers[numbers.size - 2].toInt(),
        numbers[numbers.size - 1].toInt()
    )
}

fun addBirthDayEventFromContactPhone(name: String, birthDay: LocalDate, id: Long): EventData {
    return EventData(
        EventType.BIRTHDAY,
        PeriodType.YEAR,
        birthDay,
        getCelebrationDateForBirthDay(birthDay),
        null,
        null,
        name,
        id,
        EventSourceType.CONTACTS
    )
}

fun addBirthDayEventFromLocalEdit(
    name: String, day: Int, month: Int, year: Int?,
    minutesBeforeNotification: Int?,
    sourceId: Long = 0
): EventData =
    with(LocalDate.of(year ?: MAX_YEAR, month, day)) {
        EventData(
            EventType.BIRTHDAY,
            PeriodType.YEAR,
            this,
            getCelebrationDateForBirthDay(this),
            null,
            minutesBeforeNotification?.let { LocalTime.of(it / 60, it % 60) },
            name,
            sourceId,
            EventSourceType.LOCAL
        )
    }

fun addHolidayEventFromLocalEdit(
    period: PeriodType?,
    name: String, day: Int, month: Int,
    year: Int, hour: Int?, minute: Int?,
    minutesBeforeNotification: Int?,
    sourceId: Long = 0
): EventData =
    EventData(
        EventType.HOLIDAY,
        period,
        null,
        LocalDate.of(year, month, day),
        hour?.let { minute?.let { LocalTime.of(hour, minute) } },
        minutesBeforeNotification?.let { LocalTime.of(it / 60, it % 60) },
        name,
        sourceId,
        EventSourceType.LOCAL
    )

fun addSimpleEventFromLocalEdit(
    period: PeriodType?,
    name: String, day: Int, month: Int,
    year: Int, hour: Int?, minute: Int?,
    minutesBeforeNotification: Int?,
    sourceId: Long = 0
): EventData =
    EventData(
        EventType.SIMPLE,
        period,
        null,
        LocalDate.of(year, month, day),
        hour?.let { minute?.let { LocalTime.of(hour, minute) } },
        minutesBeforeNotification?.let { LocalTime.of(it / 60, it % 60) },
        name,
        sourceId,
        EventSourceType.LOCAL
    )

fun addEventFromCalendar(
    name: String,
    startDate: Long,
    eventType: EventType,
    id: Long,
    period: PeriodType?
): EventData {
    val date =
        LocalDateTime.ofInstant(Instant.ofEpochSecond(startDate / 1000), ZoneId.systemDefault())
    return EventData(
        eventType,
        period,
        null,
        date.toLocalDate(),
        date.toLocalTime(),
        LocalTime.parse("00:15:00"),
        name,
        id,
        EventSourceType.CALENDAR
    )
}

fun deleteDuplicateEvents(eventList: MutableList<EventData>): List<EventData> {
    for (i in 0..eventList.size - 2) {
        if (eventList[i].type == EventType.BIRTHDAY) {
            for (j in i + 1..eventList.size - 1) {
                if (eventList[i].type == eventList[j].type) {
                    if ((eventList[i].name.contains(eventList[j].name)) or ((eventList[j].name.contains(
                            eventList[i].name
                        )))
                    ) eventList.removeAt(j)
                }
            }
        }
    }
    return eventList.toList()
}

fun isNewEvent(
    eventNotificationList: MutableList<EventNotificationData>,
    event: EventData
): Boolean {
    for (eventNotifi in eventNotificationList) {
        if ((eventNotifi.date == event.date) && (eventNotifi.time == event.time)
            && (eventNotifi.name == event.name)) return false
    }
    return true
}

fun addNotificationEventFromEvent(event: EventData): EventNotificationData =
    EventNotificationData(
        null,
        null,
        event.type,
        event.period,
        event.birthday,
        event.date,
        event.time,
        event.timeNotifications,
        event.name,
        event.sourceId,
        event.sourceType
    )

fun addEventsListToNotificationEventsList(
    eventNotificationList: MutableList<EventNotificationData>,
    eventList: MutableList<EventData>
) {
    for (event in eventList) {
        if (isNewEvent(eventNotificationList, event)) {
            eventNotificationList.add(addNotificationEventFromEvent(event))
        }
    }

}

fun renewNotificationEventsList(
    eventNotificationList: MutableList<EventNotificationData>,
    eventList: MutableList<EventData>
) {
    val newNotificationList = mutableListOf<EventNotificationData>()
    for (eventData in eventList) {

        eventNotificationList.find { it.date == eventData.date && it.time == eventData.time
                && it.name == eventData.name }?.let {
            newNotificationList.add(it)
        }
    }
    addEventsListToNotificationEventsList(newNotificationList, eventList)
    eventNotificationList.clear()
    eventNotificationList.addAll(newNotificationList)
}