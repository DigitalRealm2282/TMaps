package com.tdi.tmaps.model

import androidx.annotation.Keep

@Keep
class FuelStation {
    var name: String ? = null
    var Lat: Double ? = null
    var Long: Double ? = null
    var userId:String ? =null
    var id:String ?= null

    constructor()

    constructor(name: String, lat: Double,long :Double, userId: String, Id: String) : this() {
        this.name = name
        this.Lat = lat
        this.Long = long
        this.userId = userId
        this.id = Id
    }
}