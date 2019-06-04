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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.xwiki.android.sync.Constants
import org.xwiki.android.sync.utils.SharedPrefsUtils
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Will help to contain and separate functionality of working with API
 *
 * @version $Id: 5f6167ab1821c765e6c4f42549b4447481e37cbc $
 */
class BaseApiManager {

    /**
     * Main services which provide work with auth, getting groups/users and other
     */
    /**
     * @return [.xWikiServices]
     */
    val xwikiServicesApi: XWikiServices

    /**
     * Helper which work with downloading and managing of photos
     *
     * @since 0.4
     */
    /**
     * @return [.xWikiPhotosManager]
     *
     * @since 0.4
     */
    val xWikiPhotosManager: XWikiPhotosManager

    /**
     * Base constructor which create [OkHttpClient] with [XWikiInterceptor] which will
     * be used in [Retrofit] for correct handling of requests.
     *
     * @param baseUrl Url which will be used as base for all requests in this manager. Can be:
     *
     *  * http://www.xwiki.org/xwiki/
     *  * http://some.site.url/xwiki/
     *  * http://123.231.213.132/xwiki/
     *  * http://123.231.213.132:123456/xwiki/
     *
     * Strongly recommended to end url with **/

    constructor(baseUrl: String) {
        var baseUrl = baseUrl

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(XWikiInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

        // Check that url ends with `/` and put it if not
        if (!baseUrl.endsWith("/")) {
            baseUrl = "$baseUrl/"
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(okHttpClient)
            .build()

        xwikiServicesApi = initXWikiServices(retrofit)
        xWikiPhotosManager = initXWikiPhotosManager(okHttpClient, baseUrl)
    }

    /**
     * Additional constructor which will try to get base url from [SharedPrefsUtils] using
     * context and [Constants.SERVER_ADDRESS] preference name
     *
     * @param context Will be used to get info from shared preferences
     */
    constructor(context: Context) : this(SharedPrefsUtils.getValue(context, Constants.SERVER_ADDRESS, null)) {}

    /**
     * Create [XWikiServices] using given [Retrofit] instance
     *
     * @param retrofit Context for creation object
     * @return Created object
     * @see Retrofit.create
     * @since 0.4
     */
    private fun initXWikiServices(
        retrofit: Retrofit
    ): XWikiServices {
        return retrofit.create(XWikiServices::class.java)
    }

    /**
     * Create [XWikiPhotosManager] using given [OkHttpClient] client and base url
     *
     * @param client Will be used as one point of requests for result instance
     * @param baseUrl Will be used as root for all requests
     * @return Instance
     * @see XWikiPhotosManager.XWikiPhotosManager
     * @since 0.4
     */
    private fun initXWikiPhotosManager(
        client: OkHttpClient,
        baseUrl: String
    ): XWikiPhotosManager {
        return XWikiPhotosManager(client, baseUrl)
    }
}
