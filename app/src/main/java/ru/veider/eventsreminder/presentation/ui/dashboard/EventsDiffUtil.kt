package ru.veider.eventsreminder.presentation.ui.dashboard

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import ru.veider.eventsreminder.domain.EventData

class EventsDiffUtil(private val oldList: List<EventData>, private val newList: List<EventData>) :
    DiffUtil.Callback() {
    private val payload = Any()
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        try {
            val oldItem: EventData = oldList[oldItemPosition]
            val newItem: EventData = newList[newItemPosition]
            oldItem.sourceId == newItem.sourceId
                    && oldItem.sourceType == newItem.sourceType
                    //Хак для перерисовки заголовка с датой события в случае удаления первого за день события
                    && !(((oldItemPosition == 1 && newItemPosition == 0) ||
                    (oldItemPosition > 1
                    && areContentsTheSame(oldItemPosition - 2, newItemPosition - 1)))
                    && newItemPosition == oldItemPosition - 1
                    && oldItem.date == newItem.date)
        } catch (t: Throwable) {
            logErr(t)
            false
        }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        try {
            if (oldItemPosition == -1 || newItemPosition == -1)
                false
            else {
                val oldItem: EventData = oldList[oldItemPosition]
                val newItem: EventData = newList[newItemPosition]
                oldItem.name == newItem.name &&
                        oldItem.date == newItem.date &&
                        oldItem.time == newItem.time &&
                        oldItem.type == newItem.type
            }
        } catch (t: Throwable) {
            logErr(t)
            false
        }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) = payload

    private fun logErr(t: Throwable) = logErr(t, this::class.java.toString())

    private fun logErr(t: Throwable, tag: String?) {
        try {
            Log.e(tag, "", t)
        } catch (_: Throwable) {
        }
    }
}