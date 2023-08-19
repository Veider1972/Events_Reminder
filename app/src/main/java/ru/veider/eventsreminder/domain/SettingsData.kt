package ru.veider.eventsreminder.domain

import ru.veider.eventsreminder.presentation.ui.toInt
import java.time.LocalTime


data class SettingsData(
    var isDataContact: Boolean = true,
    var isDataCalendar: Boolean = true,
    var daysForShowEvents: Int = 365,
    var minutesForStartNotification: Int = 15,
    var timeToStartNotification: Int = LocalTime.of(10,10).toInt(),
    var showDateEvent: Boolean = true,
    var showTimeEvent: Boolean = true,
    var showAge: Boolean = true,
    var daysForShowEventsWidget: Int = 365,
    var widgetBackgroundColor: Int = 0x32000000,
    var widgetBorderColor: Int = 0x8BC34A,
    var widgetBorderWidth: Int = 1,
    var widgetBorderCornerRadius: Int = 16,
    var widgetLine1Color: Int = 0x33E8E6EC,
    var widgetLine2Color: Int = 0x33DCF3F3,
    var widgetFontSize: Int = 13,
    var widgetFontColorBirthday: Int = 0x01579B,
    var widgetFontColorHoliday: Int = 0x3700B3,
    var widgetFontColorSimpleEvent: Int = 0x151414,
    )
