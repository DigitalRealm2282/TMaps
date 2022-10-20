package com.tdi.tmaps.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tdi.tmaps.network.model.GeocodedWaypoint
import com.tdi.tmaps.network.model.Route

class DirectionsResponse {
    @SerializedName("geocoded_waypoints")
    @Expose
    var geocodedWaypoints: List<GeocodedWaypoint>? = null

    @SerializedName("routes")
    @Expose
    var routes: List<Route>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}