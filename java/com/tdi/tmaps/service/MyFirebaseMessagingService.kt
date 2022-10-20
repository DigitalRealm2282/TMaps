package com.tdi.tmaps.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tdi.tmaps.FriendRequestActivity
import com.tdi.tmaps.R
import com.tdi.tmaps.model.User
import com.tdi.tmaps.utils.Common
import com.tdi.tmaps.utils.NotificationHelper
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
            tokens.child(user.uid).setValue(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sendNotificationWithChannel(message)
        else
            sendNotification(message)

        addRequestToUserInfo(message.data)
    }

    private fun sendNotification(message: RemoteMessage) {
        val data = message.data
        val title = "Friend Request"
        val content = "New friend request from " + data[Common.FROM_EMAIL]
        val buttonPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            Intent(this, FriendRequestActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .addAction(
                R.drawable.ic_baseline_person_add_24,
                "Open",
                buttonPendingIntent
            )
            .setAutoCancel(false)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(), builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotificationWithChannel(message: RemoteMessage) {
        val data = message.data
        val title = "Friend Request"
        val content = "New friend request from " + data[Common.FROM_EMAIL]
        val buttonPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            Intent(this, FriendRequestActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val helper = NotificationHelper(this)
        val builder: Notification.Builder = helper.getRealtimeTrackingNotification(title, content)
            .setContentIntent(buttonPendingIntent)
//            .addAction(
//                R.drawable.ic_baseline_person_add_24,
//                "Open",
//                buttonPendingIntent
//            )

        helper.getManager().notify(Random().nextInt(), builder.build())
    }

    private fun addRequestToUserInfo(data: Map<String, String>) {
        // Pending Request
        val friendRequest = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(data[Common.TO_UID]!!)
            .child(Common.FRIEND_REQUEST)

        val user = User(data[Common.FROM_UID]!!, data[Common.FROM_EMAIL]!!)
        friendRequest.child(user.uid!!).setValue(user)
    }

    companion object {
        private const val NOTIFICATION_ID = 0
    }
}