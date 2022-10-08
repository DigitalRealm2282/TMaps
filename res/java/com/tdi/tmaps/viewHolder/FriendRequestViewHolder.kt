package com.tdi.tmaps.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tdi.tmaps.R

class FriendRequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    var friend_user_email: TextView
    var btn_accept : ImageView
    var btn_decline:ImageView

    init {
        friend_user_email = itemView.findViewById(R.id.friend_user_email) as TextView
        btn_accept = itemView.findViewById(R.id.btn_accept) as ImageView
        btn_decline = itemView.findViewById(R.id.btn_decline) as ImageView
    }

}