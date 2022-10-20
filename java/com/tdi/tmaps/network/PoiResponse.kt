package com.tdi.tmaps.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tdi.tmaps.network.poi.Result

class PoiResponse {

    @SerializedName("html_attributions")
    @Expose
    var htmlAttributions: List<Any>? = null

    @SerializedName("next_page_token")
    @Expose
    var nextPageToken: String? = null

    @SerializedName("results")
    @Expose
    var results: List<Result>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}
