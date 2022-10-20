package com.tdi.tmaps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Duration {
    @SerializedName("text")
    @Expose
    var text: String? = null

    @SerializedName("value")
    @Expose
    var value = 0
}