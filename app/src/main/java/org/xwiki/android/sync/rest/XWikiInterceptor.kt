/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync.rest

import android.content.Context
import android.text.TextUtils

import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.Constants
import org.xwiki.android.sync.utils.SharedPrefsUtils

import java.io.IOException

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.Response

/**
 * Must be used for each [okhttp3.OkHttpClient] which you will create in
 * [BaseApiManager] bounds.
 *
 * @version $Id: cc8dc74971bdd601a90f30d68102c0f042d71cb3 $
 */
class XWikiInterceptor : Interceptor {

    /**
     * @return [] with key
     * [#COOKIE()][Constants] and def value **empty string**
     *
     * @since 0.4
     */
    private val cookie: String
        get() = SharedPrefsUtils.getValue(
                AppContext.instance!!.applicationContext,
                Constants.COOKIE,
                ""
        )

    /**
     * Add query parameter **media=json**, headers [.HEADER_ACCEPT]=[.CONTENT_TYPE]
     * and [.HEADER_CONTENT_TYPE]=[.CONTENT_TYPE], also of [.getCookie] will
     * not be null - header [.HEADER_COOKIE] will be added with value from
     * [.getCookie].
     *
     * @param chain
     * @return Response from server with parameters
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val chainRequest = chain.request()

        val originalHttpUrl = chainRequest.url()
        val url = originalHttpUrl.newBuilder()
                .addQueryParameter("media", "json")
                .build()

        val builder = chainRequest.newBuilder()
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                .header(HEADER_ACCEPT, CONTENT_TYPE)
                .url(url)

        val cookie = cookie

        if (!TextUtils.isEmpty(cookie)) {
            builder.addHeader(HEADER_COOKIE, cookie)
        }

        val request = builder.build()
        return chain.proceed(request)
    }

    companion object {

        private val HEADER_CONTENT_TYPE = "Content-type"
        private val HEADER_ACCEPT = "Accept"

        private val HEADER_COOKIE = "Cookie"

        private val CONTENT_TYPE = "application/json"
    }
}
