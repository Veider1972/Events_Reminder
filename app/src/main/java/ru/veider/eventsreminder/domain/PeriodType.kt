package ru.veider.eventsreminder.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class PeriodType(val nameRus: String) : Parcelable {
	YEAR("год"),
	MONTH("месяц"),
	WEEK("неделю"),
	DAY("день");
	companion object {
		fun fromString(s: String) =
			when (s) {
				YEAR.nameRus -> YEAR
				MONTH.nameRus -> MONTH
				WEEK.nameRus -> WEEK
				DAY.nameRus -> DAY
				else -> null
			}
	}


	override fun toString() : String {
		return nameRus
	}

	fun getDays(): Long =
		when (this) {
			YEAR -> 365
			MONTH -> 30
			WEEK -> 7
			DAY -> 1
		}
}
fun Int.toPeriodType() =
	when (this){
		PeriodType.YEAR.ordinal -> PeriodType.YEAR
		PeriodType.MONTH.ordinal -> PeriodType.MONTH
		PeriodType.WEEK.ordinal -> PeriodType.WEEK
		else -> PeriodType.DAY
	}