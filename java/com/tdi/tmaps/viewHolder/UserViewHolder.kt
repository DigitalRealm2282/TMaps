package com.tdi.tmaps.viewHolder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tdi.tmaps.R
import com.tdi.tmaps.iInterface.IRecyclerItemClickListener

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var txt_user_email: TextView

    private lateinit var iRecyclerItemClickListener: IRecyclerItemClickListener

    fun setClick(iRecyclerItemClickListener: IRecyclerItemClickListener) {
        this.iRecyclerItemClickListener = iRecyclerItemClickListener
    }

    init {
        txt_user_email = itemView.findViewById(R.id.txt_user_email) as TextView

        itemView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        iRecyclerItemClickListener.onItemClickListener(p0!!, absoluteAdapterPosition)
    }
}