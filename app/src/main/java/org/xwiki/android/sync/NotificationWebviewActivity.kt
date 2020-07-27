package org.xwiki.android.sync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import org.xwiki.android.sync.utils.openLink

class NotificationWebviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_webview)
        val webView = findViewById<WebView>(R.id.notification_webview)
        val url = intent.getStringExtra("notification_url") ?: error("No Notification Url found!!!")
        webView.openLink(url)
    }
}
