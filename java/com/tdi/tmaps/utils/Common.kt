package com.tdi.tmaps.utils

import com.tdi.tmaps.model.*
import com.tdi.tmaps.remote.IFCMService
import com.tdi.tmaps.remote.RetrofitClient
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object Common {
    fun convertTimeStampToDate(time: Long): Date {
        return Date(Timestamp(time).time)
    }

    fun getDataFormatted(date: Date): String? {
        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()
    }

    val EV_STATION: String = "EvStation"
    var evInfo: EvStation ?= null
    val FS_STATION: String = "FsStation"
    var fsInfo: FuelStation ?= null
    val CAR_WASH: String = "CarWash"
    var washInfo: CarWash ?= null
    val CAR_SERVICE: String = "CarService"
    var serviceInfo: CarService ?= null
    val CAR_TIRE: String = "CarTire"
    var tireInfo: CarTires ?= null
    var USER_INFO: String = "UserInformation"
    var loggedUser: User? = null
    var trackingUser: User? = null
    val TOKENS: String = "Tokens"
    val USER_UID_SAVE_KEY: String = "SAVE_KEY"
    val ACCEPT_LIST: String = "acceptList"
    val FROM_UID: String = "FromUid"
    val TO_UID: String = "ToUid"
    val FROM_EMAIL: String = "FromName"
    val TO_EMAIL: String = "ToName"
    val FRIEND_REQUEST: String = "FriendRequestActivity"
    val PUBLIC_LOCATION: String = "PublicLocation"

    val fcmService: IFCMService
        get() = RetrofitClient.getClient("https://fcm.googleapis.com/")
            .create(IFCMService::class.java)
}
