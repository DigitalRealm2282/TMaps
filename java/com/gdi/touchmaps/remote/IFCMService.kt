package com.gdi.touchmaps.remote

import com.gdi.touchmaps.model.MyResponse
import com.gdi.touchmaps.model.Request
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers("Content-Type:application/json",
        "Authorization:key=AAAA5NgCZiE:APA91bHeIegBmqgJ7BgotYOI7V3JP2OGcS9ENjnc8I1Hk0a5VkFkCuX9aPsmythyr70S8dWl4phyUw_a-pp52ra5qziBkSCOMBpwnESpVe_3Q21DeSdKCFuTnyMMTIfehZEkX_Ora09Y")

    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request): Observable<MyResponse>
}