package com.tdi.tmaps.network.geocode

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Result {
    @SerializedName("address_components")
    @Expose
    var addressComponents: List<AddressComponent>? = null

    @SerializedName("formatted_address")
    @Expose
    var formattedAddress: String? = null

    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null

    @SerializedName("place_id")
    @Expose
    var placeId: String? = null

    @SerializedName("types")
    @Expose
    var types: List<String>? = null
}