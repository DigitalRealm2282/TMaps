package com.tdi.tmaps.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tdi.tmaps.network.geocode.Result

class GeocodeResponse {

    @SerializedName("results")
    @Expose
    var results: List<Result>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}