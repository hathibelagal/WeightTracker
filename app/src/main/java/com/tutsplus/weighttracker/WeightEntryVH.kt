package com.tutsplus.weighttracker

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class WeightEntryVH(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    var weightView: TextView? = itemView?.findViewById(R.id.weight_view)
    var timeView: TextView? = itemView?.findViewById(R.id.time_view)
}