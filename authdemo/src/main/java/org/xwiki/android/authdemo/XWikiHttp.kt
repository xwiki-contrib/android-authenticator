package org.xwiki.android.authdemo

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

import java.io.IOException

/**
 * XWikiHttp.
 */
object XWikiHttp {

    @Throws(IOException::class)
    fun isValidToken(server: String, authToken: String, callback: Callback) {
        val client = OkHttpClient()
        val url = "$server/bin/login/XWiki/XWikiLogin"
        val request = Request.Builder()
            .addHeader("Cookie", authToken)
            .url(url)
            .build()
        client.newCall(request).enqueue(callback)
    }
}
