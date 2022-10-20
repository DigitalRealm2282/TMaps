package com.tdi.tmaps.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {
    @GET("maps/api/directions/json?")
    fun getDirections(
        @Query("origin") origin: String?,
        @Query("destination") destination: String?,
        // @Query("travelMode") travelMode: TravelMode,
        @Query("key") key: String?

    ): Call<DirectionsResponse?>?

    @GET("/maps/api/place/nearbysearch/json?")
    fun listPOI(
        @Query("location") location: String?,
        @Query("radius") radius: Int,
        @Query("key") key: String?
    ): Call<PoiResponse?>?

    @GET("/maps/api/place/details/json?")
    fun getPlaceDetail(
        @Query("placeid") location: String?,
        @Query("key") key: String?
    ): Call<PlaceResponse?>?

    @GET("/maps/api/geocode/json?")
    fun getGecodeData(
        @Query("address") address: String?,
        @Query("key") key: String?
    ): Call<GeocodeResponse?>?

    @GET("/maps/api/geocode/json?")
    fun getRevGecodeData(
        @Query("latlng") latlng: String?,
        @Query("key") key: String?
    ): Call<GeocodeResponse?>?
}