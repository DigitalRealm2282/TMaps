package com.tdi.tmaps.viewHolder

interface IFirebaseLoadDone {
    fun onFirebaseLoadUserDone(lstEmail:List<String>)
    fun onFirebaseLoadFailed(message:String)
}