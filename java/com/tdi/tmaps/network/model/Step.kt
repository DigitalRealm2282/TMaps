package com.tdi.tmaps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Step {
    @SerializedName("distance")
    @Expose
    var distance: Distance_? = null

    @SerializedName("duration")
    @Expose
    var duration: Duration_? = null

    @SerializedName("end_location")
    @Expose
    var endLocation: EndLocation_? = null

    @SerializedName("html_instructions")
    @Expose
    var htmlInstructions: String? = null

    @SerializedName("polyline")
    @Expose
    var polyline: Polyline? = null

    @SerializedName("start_location")
    @Expose
    var startLocation: StartLocation_? = null

    @SerializedName("travel_mode")
    @Expose
    var travelMode: String? = null

    @SerializedName("maneuver")
    @Expose
    var maneuver: String? = null
}