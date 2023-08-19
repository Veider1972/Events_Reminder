package ru.veider.eventsreminder.presentation.ui.myevents

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.veider.eventsreminder.domain.EventData


class ItemTouchHelperCallback(private val mAdapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        mAdapter.onItemDismiss(viewHolder as MyEventsViewHolder)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
       return makeMovementFlags(0, ItemTouchHelper.START or ItemTouchHelper.END)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        TODO("Not yet implemented")
    }

}