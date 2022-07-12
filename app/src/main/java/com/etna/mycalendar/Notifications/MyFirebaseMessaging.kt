package com.etna.mycalendar.Notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.etna.mycalendar.Activity.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val sented: String = remoteMessage.getData().get("sented").toString()
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().getCurrentUser()
        if (firebaseUser != null && sented == firebaseUser.getUid()) {
            sendNotification(remoteMessage)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user: String? = remoteMessage.getData().get("user")
        val icon: String? = remoteMessage.getData().get("icon")
        val title: String? = remoteMessage.getData().get("title")
        val body: String? = remoteMessage.getData().get("body")
        val notification: RemoteMessage.Notification? = remoteMessage.getNotification()
        val j = user?.replace("[\\D]".toRegex(), "")?.toInt()
        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, j!!, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon?.toInt()!!)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)
        val noti: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if (j!! > 0) {
            i = j
        }
        noti.notify(i, builder.build())
    }
}