package com.gdi.touchmaps.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.gdi.touchmaps.R
import com.gdi.touchmaps.utils.Common
import com.gdi.touchmaps.utils.NotificationHelper
import com.gdi.touchmaps.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService:FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
            tokens.child(user.uid).setValue(token)
        }

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.data != null){
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
                sendNotificationWithChannel(message)
            else
                sendNotification(message)

            addRequestToUserInfo(message.data)
        }
    }

    private fun sendNotification(message: RemoteMessage) {
        val data = message.data
        val title = "Friend Request"
        val content = "New friend request from" + data[Common.FROM_EMAIL]

        val builder = NotificationCompat.Builder(this,"")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(false)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(),builder.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotificationWithChannel(message: RemoteMessage) {
        val data = message.data
        val title = "Friend Request"
        val content = "New friend request from" + data[Common.FROM_EMAIL]

        val helper = NotificationHelper(this)
        val builder: Notification.Builder = helper.getRealtimeTrackingNotification(title, content)

        helper.getManager().notify(Random().nextInt(),builder.build())
    }

    private fun addRequestToUserInfo(data: Map<String, String>) {
        //Pending Request
        val friendRequest = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(data[Common.TO_UID]!!)
            .child(Common.FRIEND_REQUEST)

        val user = User(data[Common.FROM_UID]!!,data[Common.FROM_EMAIL]!!)
        friendRequest.child(user.uid!!).setValue(user)
    }
}