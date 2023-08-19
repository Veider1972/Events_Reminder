package ru.veider.eventsreminder.presentation.ui.myevents

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.domain.EventData


class MyEventsRecyclerViewAdapter(
    private var myEvents: List<EventData>,
 	private val editor: EventEditor
) : ItemTouchHelperAdapter, RecyclerView.Adapter<MyEventsViewHolder>() {
	var selectedPos = RecyclerView.NO_POSITION

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventsViewHolder =
		MyEventsViewHolder(
			LayoutInflater.from(parent.context)
				.inflate(R.layout.my_events_recyclerview_item, parent, false)
		)

	override fun getItemCount(): Int = myEvents.size

	override fun onBindViewHolder(holder: MyEventsViewHolder, position: Int) {
		try {
			holder.bind(
				myEvents[position],
				position == 0 || myEvents[position - 1].date != myEvents[position].date,
				selectedPos == position,
				editor
			)

		} catch (t: Throwable) {
			Log.e(this::class.java.toString(), "", t)
		}
	}

	override fun onItemDismiss(myEventsViewHolder: MyEventsViewHolder) {
		editor.openRemoveEvent(myEvents[myEventsViewHolder.layoutPosition])
	}

	fun notifyItemChanged(item: EventData){
		notifyItemChanged(myEvents.indexOf(item))
	}
}