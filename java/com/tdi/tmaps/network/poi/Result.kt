package com.tdi.tmaps.network.poi

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Result {
    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null

    @SerializedName("icon")
    @Expose
    var icon: String? = null

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("photos")
    @Expose
    var photos: List<Photo>? = null

    @SerializedName("place_id")
    @Expose
    var placeId: String? = null

    @SerializedName("reference")
    @Expose
    var reference: String? = null

    @SerializedName("scope")
    @Expose
    var scope: String? = null

    @SerializedName("types")
    @Expose
    var types: List<String>? = null

    @SerializedName("vicinity")
    @Expose
    var vicinity: String? = null

    @SerializedName("rating")
    @Expose
    var rating: Float? = null

    @SerializedName("opening_hours")
    @Expose
    var openingHours: OpeningHours? = null

    @SerializedName("price_level")
    @Expose
    var priceLevel: Int? = null

    @SerializedName("aspects")
    @Expose
    var aspects: List<Aspect>? = null
}