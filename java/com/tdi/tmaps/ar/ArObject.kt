package com.tdi.tmaps.ar

import android.location.Location
import com.beyondar.android.plugin.BeyondarObjectPlugin
import com.beyondar.android.plugin.GeoObjectPlugin
import com.beyondar.android.util.math.Distance
import com.beyondar.android.world.BeyondarObject
import com.beyondar.android.world.GeoObject

/**
 * Created by Amal Krishnan on 27-03-2017.
 */
class ArObject : BeyondarObject {
    var longitude = 0.0
        private set
    var latitude = 0.0
        private set
    var altitude = 0.0
        private set
    var placeId: String? = null

    constructor(id: Long) : super(id) {
        this.isVisible = true
    }

    constructor() {
        this.isVisible = true
    }

    fun setGeoPosition(latitude: Double, longitude: Double) {
        this.setGeoPosition(latitude, longitude, altitude)
    }

    fun setGeoPosition(latitude: Double, longitude: Double, altitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        lockPlugins
        synchronized(lockPlugins) {
            val var9: Iterator<*> = plugins.iterator()
            while (var9.hasNext()) {
                val plugin = var9.next() as BeyondarObjectPlugin
                if (plugin is GeoObjectPlugin) {
                    plugin.onGeoPositionChanged(latitude, longitude, altitude)
                }
            }
        }
    }

    fun setLocation(location: Location?) {
        if (location != null) {
            this.setGeoPosition(location.latitude, location.longitude)
        }
    }

    fun calculateDistanceMeters(geo: GeoObject): Double {
        return this.calculateDistanceMeters(geo.longitude, geo.latitude)
    }

    fun calculateDistanceMeters(longitude: Double, latitude: Double): Double {
        return Distance.calculateDistanceMeters(this.longitude, this.latitude, longitude, latitude)
    }
}