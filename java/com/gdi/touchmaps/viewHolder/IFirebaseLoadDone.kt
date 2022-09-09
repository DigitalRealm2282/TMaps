package com.gdi.touchmaps.viewHolder

interface IFirebaseLoadDone {
    fun onFirebaseLoadUserDone(lstEmail:List<String>)
    fun onFirebaseLoadFailed(message:String)
}