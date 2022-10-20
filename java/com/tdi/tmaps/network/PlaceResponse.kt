package com.tdi.tmaps.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tdi.tmaps.network.place.Result

class PlaceResponse {

    @SerializedName("html_attributions")
    @Expose
    var htmlAttributions: List<Any>? = null

    @SerializedName("result")
    @Expose
    var result: Result? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}