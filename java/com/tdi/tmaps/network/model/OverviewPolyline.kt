package com.tdi.tmaps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OverviewPolyline {
    @SerializedName("points")
    @Expose
    var points: String? = null
}