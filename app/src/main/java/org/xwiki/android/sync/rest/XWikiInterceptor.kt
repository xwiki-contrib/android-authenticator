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

import android.text.TextUtils
import okhttp3.Interceptor
import okhttp3.Response
import org.xwiki.android.sync.*
import org.xwiki.android.sync.utils.SharedPrefsUtils

import java.io.IOException

private const val HEADER_CONTENT_TYPE = "Content-type"
private const val HEADER_ACCEPT = "Accept"

private const val HEADER_COOKIE = "Cookie"

private const val CONTENT_TYPE = "application/json"

/**
 * Must be used for each [okhttp3.OkHttpClient] which you will create in
 * [BaseApiManager] bounds.
 *
 * @version $Id: 374209a130ca477ae567048f6f4a129ace2ea0d1 $
 */
class XWikiInterceptor : Interceptor {

    /**
     * @return [SharedPrefsUtils.getValue] with key
     * [Constants.COOKIE] and def value **empty string**
     *
     * @since 0.4
     */
    private val cookie: String
    get() = currentXWikiAccount?.cookie.toString()
//        get() = getValue(
//            appContext.applicationContext,
//            COOKIE,
//            ""
//        )

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

}
