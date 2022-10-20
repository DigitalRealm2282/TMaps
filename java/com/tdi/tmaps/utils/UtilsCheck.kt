package com.tdi.tmaps.utils

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by Amal Krishnan on 20-12-2016.
 */
object UtilsCheck {
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}