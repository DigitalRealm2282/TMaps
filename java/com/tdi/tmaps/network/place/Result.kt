package com.tdi.tmaps.network.place

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Result {
    @SerializedName("address_components")
    @Expose
    var addressComponents: List<AddressComponent>? = null

    @SerializedName("adr_address")
    @Expose
    var adrAddress: String? = null

    @SerializedName("formatted_address")
    @Expose
    var formattedAddress: String? = null

    @SerializedName("formatted_phone_number")
    @Expose
    var formattedPhoneNumber: String? = null

    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null

    @SerializedName("icon")
    @Expose
    var icon: String? = null

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("international_phone_number")
    @Expose
    var internationalPhoneNumber: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("opening_hours")
    @Expose
    var openingHours: OpeningHours? = null

    @SerializedName("photos")
    @Expose
    var photos: List<Photo>? = null

    @SerializedName("place_id")
    @Expose
    var placeId: String? = null

    @SerializedName("rating")
    @Expose
    var rating: Double? = null

    @SerializedName("reference")
    @Expose
    var reference: String? = null

    @SerializedName("reviews")
    @Expose
    var reviews: List<Review>? = null

    @SerializedName("scope")
    @Expose
    var scope: String? = null

    @SerializedName("types")
    @Expose
    var types: List<String>? = null

    @SerializedName("url")
    @Expose
    var url: String? = null

    @SerializedName("utc_offset")
    @Expose
    var utcOffset: Int? = null

    @SerializedName("vicinity")
    @Expose
    var vicinity: String? = null

    @SerializedName("website")
    @Expose
    var website: String? = null
}