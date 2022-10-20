package com.tdi.tmaps.utils


object LocationCalc {
    private const val EarthRadius = 6371.0

    //    private class LatLng{
    //        double lat;
    //        double lng;
    //
    //        LatLng(double lat,double lng){
    //            this.lat=lat;
    //            this.lng=lng;
    //        }
    //
    //        public double getLat(){
    //            return lat;
    //        }
    //
    //        public double getLng(){
    //            return lng;
    //        }
    //    }
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        var lat1 = lat1
        var lat2 = lat2
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)
        val a = Math.pow(Math.sin(dLat / 2), 2.0) + Math.pow(
            Math.sin(dLon / 2),
            2.0
        ) * Math.cos(lat1) * Math.cos(lat2)
        val c = 2 * Math.asin(Math.sqrt(a))
        return EarthRadius * c
    }

    fun calcBearing(lat1_D: Double, lat2_D: Double, lng1_D: Double, lng2_D: Double): Double {
        val lat1_R = Math.toRadians(lat1_D)
        val lat2_R = Math.toRadians(lat2_D)
        val lng1_R = Math.toRadians(lng1_D)
        val lng2_R = Math.toRadians(lng2_D)
        val y = Math.sin(lng2_R - lng1_R) * Math.cos(lat2_R)
        val x =
            Math.cos(lat1_R) * Math.sin(lat2_R) - Math.sin(lat1_R) * Math.cos(lat2_R) * Math.cos(
                lng2_R - lng1_R
            )
        return Math.toDegrees(Math.atan2(y, x))
    }

    fun calcLatLngfromBearing(lat1_D: Double, lng1_D: Double, bear: Double, d: Double): LatLng {
        val lat1_R = Math.toRadians(lat1_D)
        val lng1_R = Math.toRadians(lng1_D)
        val lat2_R = Math.asin(
            Math.sin(lat1_R) * Math.cos(d / EarthRadius) +
                Math.cos(lat1_R) * Math.sin(d / EarthRadius) * Math.cos(bear)
        )
        val lng2_R = lng1_R + Math.atan2(
            Math.sin(bear) * Math.sin(d / EarthRadius) * Math.cos(lat1_R),
            Math.cos(d / EarthRadius) - Math.sin(lat1_R) * Math.sin(lat2_R)
        )
        return LatLng(Math.toDegrees(lat2_R), Math.toDegrees(lng2_R))
    }
}