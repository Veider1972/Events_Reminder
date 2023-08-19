package ru.veider.eventsreminder.presentation.ui.myevents

import ru.veider.eventsreminder.domain.EventData

interface ItemTouchHelperAdapter {
    fun onItemDismiss(myEventsViewHolder: MyEventsViewHolder)
}