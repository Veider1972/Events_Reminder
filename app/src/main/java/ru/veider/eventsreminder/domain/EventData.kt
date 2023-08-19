package ru.veider.eventsreminder.domain

import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class EventData(
    val type: EventType,
    val period: PeriodType?,
    val birthday:LocalDate?,
    val date: LocalDate,
    val time: LocalTime?,
    val timeNotifications:LocalTime?,
    val name:String,
    val sourceId:Long,
    val sourceType: EventSourceType
) : Parcelable{

    /**
     * Размножить событие исходя из периода повторений за заданный промежуток времени
     * @param from время, начиная с которого разрешено размножать события
     * @param to время, до которого разрешено размножать события
     * @return список размноженных событий за за заданный промежуток включая само событие
     * */
    fun breedPeriodicEvents(from: LocalDate, to: LocalDate) : List<EventData> {
        val retEvents = mutableListOf<EventData>()
        retEvents.add(this)
        try{
            var curDate = date
            period?.let{
                do{
                    curDate = when(it){
                        PeriodType.YEAR -> curDate.plusYears(1)
                        PeriodType.MONTH -> curDate.plusMonths(1)
                        PeriodType.WEEK -> curDate.plusWeeks(1)
                        PeriodType.DAY -> curDate.plusDays(1)
                    }
                    if (curDate >= from && curDate <= to)
                        retEvents.add(this.copy(date = curDate))
                }while (curDate <= to)
            }
        }catch(t:Throwable){
            Log.e(this::class.java.toString(),"",t)
        }
        return retEvents
    }
}
