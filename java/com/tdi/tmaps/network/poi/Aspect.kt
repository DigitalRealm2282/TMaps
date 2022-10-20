package com.tdi.tmaps.network.poi

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Aspect {
    @SerializedName("rating")
    @Expose
    var rating: Int? = null

    @SerializedName("type")
    @Expose
    var type: String? = null
}