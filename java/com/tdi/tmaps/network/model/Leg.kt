package com.tdi.tmaps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Leg {
    @SerializedName("distance")
    @Expose
    var distance: Distance? = null

    @SerializedName("duration")
    @Expose
    var duration: Duration? = null

    @SerializedName("end_address")
    @Expose
    var endAddress: String? = null

    @SerializedName("end_location")
    @Expose
    var endLocation: EndLocation? = null

    @SerializedName("start_address")
    @Expose
    var startAddress: String? = null

    @SerializedName("start_location")
    @Expose
    var startLocation: StartLocation? = null

    @SerializedName("steps")
    @Expose
    var steps: List<Step>? = null

    @SerializedName("traffic_speed_entry")
    @Expose
    var trafficSpeedEntry: List<Any>? = null

    @SerializedName("via_waypoint")
    @Expose
    var viaWaypoint: List<Any>? = null
}