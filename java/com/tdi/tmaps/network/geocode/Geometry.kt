package com.tdi.tmaps.network.geocode

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Geometry {
    @SerializedName("bounds")
    @Expose
    var bounds: Bounds? = null

    @SerializedName("location")
    @Expose
    var location: Location? = null

    @SerializedName("location_type")
    @Expose
    var locationType: String? = null

    @SerializedName("viewport")
    @Expose
    var viewport: Viewport? = null
}