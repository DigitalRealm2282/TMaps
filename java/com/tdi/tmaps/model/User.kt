package com.tdi.tmaps.model


class User {
    var uid:String ?=null
    var email:String ?= null
    private var acceptList:HashMap<String, User>?=null

    constructor()

    constructor(uid: String , email: String) : this() {
        this.uid = uid
        this.email = email
        acceptList = HashMap()
    }
}
