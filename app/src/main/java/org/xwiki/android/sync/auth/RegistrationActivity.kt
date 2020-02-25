package org.xwiki.android.sync.auth

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import org.xwiki.android.sync.R
import org.xwiki.android.sync.utils.openLinkInWebView

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        val webView = findViewById(R.id.webview) as WebView
        val url = intent.getStringExtra("url")
        val intent = openLinkInWebView(
                url, webView
        )
    }
}