package ru.veider.eventsreminder.usecases

import ru.veider.eventsreminder.domain.EventData
import ru.veider.eventsreminder.domain.EventDto
import ru.veider.eventsreminder.domain.toEventSourceType
import ru.veider.eventsreminder.domain.toEventType
import ru.veider.eventsreminder.domain.toPeriodType
import java.time.LocalDate
import java.time.LocalTime

fun EventData.toEventDto(): EventDto =
	EventDto(
		type = type.ordinal,
		period = period?.ordinal,
		birthday = birthday?.toEpochDay(),
		date = date.toEpochDay(),
		time = time?.toSecondOfDay(),
		timeNotifications = timeNotifications?.toSecondOfDay(),
		name = name,
		sourceId = sourceId,
		sourceType = sourceType.ordinal
	)

fun EventDto.toEventData(): EventData =
	EventData(
		type = type.toEventType(),
		period = period?.toPeriodType(),
		birthday = birthday?.let{ LocalDate.ofEpochDay(it)},
		date = LocalDate.ofEpochDay(date),
		time = time?.let{ LocalTime.ofSecondOfDay(it.toLong())},
		timeNotifications = timeNotifications?.let{ LocalTime.ofSecondOfDay(it.toLong())},
		name = name,
		sourceId = sourceId,
		sourceType = sourceType.toEventSourceType()
	)