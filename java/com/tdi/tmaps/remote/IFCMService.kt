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
        "Authorization: key=AAAAvH2ruLE:APA91bHXcABrWCk0Eo5Va4vkpMUjZNbaV2zoO0OiglTPZrHSEg2FMkSm_t3VPJKrPArsec5FhVDbw3-f_mPXHPCnNsJ36LzRQjo7AT20t1BAa-bsHsq_8v7SxtD9MPGRXygxR1-0X9Uj"
    )

    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request): Observable<MyResponse>
}