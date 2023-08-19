package ru.veider.eventsreminder.presentation.ui.myevents

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller


class CenterLayoutManager : LinearLayoutManager {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        try {
            val smoothScroller: SmoothScroller = CenterSmoothScroller(recyclerView.context)
            smoothScroller.targetPosition = position
            startSmoothScroll(smoothScroller)
        }catch(t:Throwable){
            Log.e(this::class.java.toString(), "", t)}
    }

    private class CenterSmoothScroller(context: Context?) :
        LinearSmoothScroller(context) {
        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {
            try {
                return boxStart + (boxEnd - boxStart) / 2 - (viewStart + (viewEnd - viewStart) / 2)
            }catch(t:Throwable){Log.e(this::class.java.toString(), "", t)
                return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference)
            }
        }
    }
}

