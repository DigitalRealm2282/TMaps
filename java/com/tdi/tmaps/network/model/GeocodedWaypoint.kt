package com.tdi.tmaps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GeocodedWaypoint {
    @SerializedName("geocoder_status")
    @Expose
    var geocoderStatus: String? = null

    @SerializedName("place_id")
    @Expose
    var placeId: String? = null

    @SerializedName("types")
    @Expose
    var types: List<String>? = null
}