package com.tdi.tmaps.remote

import com.tdi.tmaps.model.MyResponse
import com.tdi.tmaps.model.Request
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers("Content-Type:application/json",
        "Authorization: key=AAAAVK8-OlA:APA91bHsX9iTB8GUqFFYjVHNnKeOAJSX7f9_BJ0bfclK_NzsX0PD4PsykNygvgBTGIEJmrZjnpzexRvyLroVCY_dENaO-EdyeTJyjRH0NCuuiCahgG4qNAk69PksadBwbOQeC4Rg2lsj")

    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request): Observable<MyResponse>
}