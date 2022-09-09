package com.gdi.touchmaps.utils

import com.gdi.touchmaps.model.User
import com.gdi.touchmaps.remote.IFCMService
import com.gdi.touchmaps.remote.RetrofitClient
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object Common{
    fun convertTimeStampToDate(time: Long): Date {
        return Date(Timestamp(time).time)
    }

    fun getDataFormatted(date: Date): String? {
        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()
    }

    var USER_INFO:String = "UserInformation"
    var loggedUser: User?= null
    var trackingUser: User?=null
    val TOKENS:String = "Tokens"
    val USER_UID_SAVE_KEY:String="SAVE_KEY"
    val ACCEPT_LIST:String = "acceptList"
    val FROM_UID:String="FromUid"
    val TO_UID:String="ToUid"
    val FROM_EMAIL:String="FromName"
    val TO_EMAIL:String="ToName"
    val FRIEND_REQUEST:String="FriendRequestActivity"
    val PUBLIC_LOCATION:String="PublicLocation"

    val fcmService: IFCMService
        get() = RetrofitClient.getClient("https://fcm.googleapis.com/")
            .create(IFCMService::class.java)


}
