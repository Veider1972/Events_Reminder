package ru.veider.eventsreminder.presentation.ui

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Перевести в словесное описание дней  ("-10 дней", "Вчера", "Сегодня", "Завтра", "+ 3 дня")
 * */
fun LocalDate.toDaysSinceNowInWords() =
    ChronoUnit.DAYS.between(LocalDate.now(), this).toInt().let {
        when (it) {
            -2 -> "Позавчера"
            -1 -> "Вчера"
            0 -> "Сегодня"
            1 -> "Завтра"
            2 -> "Послезавтра"
            else -> (if (it > 2) " +" else " -") + RusIntPlural(
                "д",
                kotlin.math.abs(it),
                "ень", "ня", "ней"
            ).toString()
        }
    }

fun LocalDate.toShortDaysSinceNowInWords() =
    ChronoUnit.DAYS.between(LocalDate.now(), this).toInt().let {
        when (it) {
            0 -> "Сегодня"
            1 -> "Завтра"
            else -> "${it} дн."
        }
    }

/**
 * Считая текущую дату днём рождения, вывести в словесной форме возраст к заданной дате
 * ("1 год", "5 лет", "3 года")
 * @param [dateSinceBirthday] дата, к которой выводить возраст
 * */
fun LocalDate.toAgeInWordsByDate(dateSinceBirthday: LocalDate) =
    RusIntPlural(
        "",
        ChronoUnit.YEARS.between(this, dateSinceBirthday).toInt(),
        "год", "года", "лет"
    ).toString()

fun LocalDate.toInt() =
    this.year * 10000 + this.month.value * 100 + this.dayOfMonth

fun LocalTime.toInt() =
    this.hour * 10000 + this.minute * 100 + this.second

fun Int.toLocalDate() =
    LocalDate.of(this / 10000, this / 100 % 100, this % 100)

fun Int.toLocalTime() =
    LocalTime.of(this / 10000, this / 100 % 100, this % 100)

fun LocalDate.safeWithYear(year : Int) =
    try{
        withYear(year)
    }catch (t:Throwable){minusDays(1).withYear(year)}

