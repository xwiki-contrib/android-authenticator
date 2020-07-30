package org.xwiki.android.sync.activities.Notifications

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.utils.openLink

private const val notificationUrlField = "notification_url"

fun Context.startNotificationWebViewActivity(url: String) = Intent(this, NotificationWebviewActivity::class.java).also {
    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    it.putExtra(notificationUrlField, url)
    startActivity(it)
}

class NotificationWebviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_webview)
        val webView = findViewById<WebView>(R.id.notification_webview)
        val url = intent.getStringExtra(notificationUrlField) ?: error("No Notification Url found!!!")
        webView.openLink(url)
    }
}
