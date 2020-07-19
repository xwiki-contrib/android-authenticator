package org.xwiki.android.sync.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import org.xwiki.android.sync.R

fun Context.createNotification(title: String, description: String) {

    var notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel =
            NotificationChannel("101", "xwiki notifications", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    val notificationBuilder = NotificationCompat.Builder(applicationContext, "101")
        .setContentTitle(title)
        .setContentText(description)
        .setSmallIcon(R.mipmap.ic_launcher)

    notificationManager.notify(1, notificationBuilder.build())

}