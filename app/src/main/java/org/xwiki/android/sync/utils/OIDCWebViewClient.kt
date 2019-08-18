package org.xwiki.android.sync.utils

import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import org.xwiki.android.sync.activities.OIDC.WebViewPageLoadedListener

internal class OIDCWebViewClient(private val webViewPageLoadedListener: WebViewPageLoadedListener, private val accountName: String) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        if (!Uri.parse(url).getQueryParameter("code").isNullOrEmpty()) {
            val authorizationCode = Uri.parse(url).getQueryParameter("code")
            view?.destroy()
            view?.visibility  = View.GONE
            webViewPageLoadedListener.onPageLoaded(authorizationCode, accountName)
        }
    }
}