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

import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.abstracts.UserAccountsCookiesRepository
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
class XWikiInterceptor(
    private val userAccountId: UserAccountId,
    private val userAccountsCookiesRepository: UserAccountsCookiesRepository,
    private val authToken: String?
) : Interceptor {

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

        var cookie: String? = null

        appCoroutineScope.launch {
            cookie = userAccountsCookiesRepository[userAccountId]
        }

        val originalHttpUrl = chainRequest.url()
        val url = originalHttpUrl.newBuilder()
            .addQueryParameter("media", "json")
            .build()

        val builder = if(authToken.isNullOrEmpty()) {
            chainRequest
                .newBuilder()
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                .header(HEADER_ACCEPT, CONTENT_TYPE)
                .header(HEADER_COOKIE, cookie.toString())
                .url(url)
        } else {
            chainRequest
                .newBuilder()
                .addHeader("Authorization", "Bearer $authToken")
                .url(url)
        }

        val request = builder.build()
        return chain.proceed(request)
    }
}
