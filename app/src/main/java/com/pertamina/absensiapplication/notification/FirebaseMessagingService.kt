package com.pertamina.absensiapplication.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pertamina.absensiapplication.repository.PermitRepository

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val tag= PermitRepository::class.java.simpleName

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Log.d(tag, "From: " + remoteMessage?.from)
        if (remoteMessage != null) {

            if (remoteMessage.data.isNotEmpty()) {
                val data = remoteMessage.data
                Log.d(tag, "Message data payload: $data")
                val title = data.getValue("title")
                val body = data.getValue("body")
                val image = data.getValue("image")
                NotificationHelper.run { displayNotification(applicationContext, title, body, image) }
            }
        }
        // Check if message contains a notification payload.
        if (remoteMessage?.notification != null) {
            Log.d(tag, "Message Notification Body: " + remoteMessage.notification?.body)
        }
    }
}