package com.tdi.tmaps.iInterface

import android.view.View

interface IRecyclerItemClickListener {
    fun onItemClickListener(view: View, position: Int)
}