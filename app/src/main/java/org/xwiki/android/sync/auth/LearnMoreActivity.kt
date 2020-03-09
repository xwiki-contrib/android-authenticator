package org.xwiki.android.sync.auth

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import org.xwiki.android.sync.R
import org.xwiki.android.sync.defaultLearnMoreLink
import org.xwiki.android.sync.utils.openLink
import android.webkit.WebViewClient

class LearnMoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_more)
        val webView = findViewById<WebView>(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.openLink(defaultLearnMoreLink)
    }
}
