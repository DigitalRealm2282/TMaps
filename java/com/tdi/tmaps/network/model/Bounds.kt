package com.tdi.tmaps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Bounds {
    @SerializedName("northeast")
    @Expose
    var northeast: Northeast? = null

    @SerializedName("southwest")
    @Expose
    var southwest: Southwest? = null
}