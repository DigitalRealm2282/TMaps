package com.tdi.tmaps.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tdi.tmaps.utils.Common
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper

class MyLocationReceiver :BroadcastReceiver(){
    private var publicLocation:DatabaseReference = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
    private lateinit var uid:String

    companion object{
        val ACTION = "com.tdi.tmaps.UPDATE_LOCATION"

    }

    override fun onReceive(p0: Context?, intent: Intent?) {
        Paper.init(p0!!)
        uid = Paper.book().read<String>(Common.USER_UID_SAVE_KEY).toString()

        if (intent != null)
        {
            val action = intent.action
            if (action == ACTION)
            {
                val result = LocationResult.extractResult(intent)
                if (result != null)
                {
                    val location = result.lastLocation
                    if (Common.loggedUser !=null)
                    {
                        // App running || user online
                        publicLocation.child(Common.loggedUser!!.uid!!).setValue(location)
                    }else
                    {
                        // App in kill mode || app in background || user offline
                        publicLocation.child(uid).setValue(location)
                    }
                }
            }
        }
    }

}