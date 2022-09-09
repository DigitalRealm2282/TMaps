package com.gdi.touchmaps.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.gdi.touchmaps.R

class NotificationHelper(base:Context):ContextWrapper(base) {

    companion object{
        private val TOUCH_CHANNEL_ID = "TOUCH_MAP_ID"
        private val TOUCH_CHANNEL_NAME = "TOUCH_MAP"
    }


    private var manager:NotificationManager ?= null
    init {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
            createChannel(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(defaultUri: Uri?) {
        val touchChannel = NotificationChannel(TOUCH_CHANNEL_ID, TOUCH_CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT)

        touchChannel.enableVibration(true)
        touchChannel.enableLights(true)

        touchChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val audioAttribute = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()

        touchChannel.setSound(defaultUri,audioAttribute)
        getManager().createNotificationChannel(touchChannel)
    }


    fun getManager():NotificationManager{
        if (manager == null)
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return manager!!
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getRealtimeTrackingNotification(title:String, content:String): Notification.Builder{
        return Notification.Builder(applicationContext,TOUCH_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(false)
    }
}