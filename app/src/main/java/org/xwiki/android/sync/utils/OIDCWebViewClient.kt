package org.xwiki.android.sync.utils

import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class OIDCWebViewClient(private val webViewPageLoadedListener: WebViewPageLoadedListener) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        if (!Uri.parse(url).getQueryParameter("code").isNullOrEmpty()) {
            val authorizationCode = Uri.parse(url).getQueryParameter("code")
            view?.destroy()
            view?.visibility  = View.GONE
            webViewPageLoadedListener.onPageLoaded(authorizationCode)
        }
    }
}

interface WebViewPageLoadedListener {
    fun onPageLoaded(authorizationCode: String?)
}

suspend fun checkOIDCSupport(
    serverUrl: String,
    retries: Int = 3
): Boolean {
    val supported = try {
        val url = URL ("${serverUrl}/oidc/userinfo")

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()

        val call = client.newCall(request)

        val response = suspendCoroutine<Response> { it.resume(call.execute()) }

        when {
            response.code().let { it == 404 || it == 401 } -> return false
            response.code() == 500 -> return true
            else -> false
        }
    } catch (e: Exception) {
        Log.d("OIDC Support checker", "checking OIDC support", e)
        false
    }

    return supported || if (retries > 0) {
        delay(1000)
        checkOIDCSupport(serverUrl, retries - 1)
    } else {
        supported
    }
}