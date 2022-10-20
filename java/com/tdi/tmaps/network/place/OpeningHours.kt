package com.tdi.tmaps.network.place

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OpeningHours {
    @SerializedName("open_now")
    @Expose
    var openNow: Boolean? = null

    @SerializedName("periods")
    @Expose
    var periods: List<Period>? = null

    @SerializedName("weekday_text")
    @Expose
    var weekdayText: List<String>? = null
}