package com.tdi.tmaps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Southwest {
    @SerializedName("lat")
    @Expose
    var lat = 0f

    @SerializedName("lng")
    @Expose
    var lng = 0f
}