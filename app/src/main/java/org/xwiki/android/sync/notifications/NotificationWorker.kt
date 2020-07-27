package org.xwiki.android.sync.notifications

import android.accounts.AccountManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.Notifications.NotificationsActivity
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.resolveApiManager
import org.xwiki.android.sync.userAccountsRepo
import java.util.Random

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        return try {
            val username = inputData.getString("username")
            if (!username.isNullOrEmpty())
                getNotifications(username)
            Result.success()
        } catch (t: Throwable) {
            Log.e("NotificationWorker", t.message)
            Result.failure()
        }
    }

    private fun createNotification(title: String, description: String, username: String?) {

        var notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("101", "xwiki_channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "101")
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent(applicationContext,username))
            .addAction(R.mipmap.ic_launcher,"View",pendingIntent(applicationContext,username))

        val random = Random()
        
        notificationManager.notify(random.nextInt(100000), notificationBuilder.build())

    }

    private fun pendingIntent(context: Context, username: String?): PendingIntent {
        val intent = Intent(context, NotificationsActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username)
        return PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getNotifications(username: String) {
        appCoroutineScope.launch {
            val userAccount =
                userAccountsRepo.findByAccountName(username) ?: return@launch
            val apiManager = resolveApiManager(userAccount)
            val userId = "xwiki" + ":" + "XWiki" + "." + username
            apiManager.xwikiServicesApi.getNotify(userId, true)
                .subscribe(
                    {
                        it.notifications.forEach {
                            Log.e("NotificationActivity", it.document.toString() + it.type.toString())
                            createNotification(it.type.toString(), it.document.toString(), username)
                        }
                    },
                    {
                        Log.e("Error", it.message)
                    }
                )
        }
    }
}