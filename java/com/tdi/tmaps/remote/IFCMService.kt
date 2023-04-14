package com.tdi.tmaps.remote

import com.tdi.tmaps.model.MyResponse
import com.tdi.tmaps.model.Request
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization: key=AAAA"
    )

    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request): Observable<MyResponse>
}
