package ru.veider.eventsreminder.presentation.ui.dashboard

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.veider.eventsreminder.R.layout.dashboard_recyclerview_item
import ru.veider.eventsreminder.domain.EventData

class DashboardRecyclerViewAdapter(var events: List<EventData>) :
    RecyclerView.Adapter<DashboardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder =
        DashboardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(dashboard_recyclerview_item, parent, false)
        )

    override fun getItemCount(): Int = events.size
    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        try {
            holder.bind(
                events[position],
                position == 0 || events[position - 1].date != events[position].date
            )
        } catch (t: Throwable) {
            Log.e(DashboardRecyclerViewAdapter::class.java.toString(), "", t)
        }
    }
    fun applyDiffResult(
        newList: List<EventData>, oldList: MutableList<EventData>
    ) {
        val diffResult = DiffUtil.calculateDiff(
            EventsDiffUtil(
                oldList,
                newList
            )
        )
        oldList.clear()
        oldList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
