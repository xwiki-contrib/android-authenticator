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
package org.xwiki.android.sync.rest;

import android.content.Context;
import android.support.annotation.NonNull;

import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Will help to contain and separate functionality of working with API
 *
 * @version $Id$
 */
public class BaseApiManager {

    /**
     * Main services which provide work with auth, getting groups/users and other
     */
    private final XWikiServices xWikiServices;

    /**
     * Helper which work with downloading and managing of photos
     *
     * @since 0.4
     */
    private final XWikiPhotosManager xWikiPhotosManager;

    /**
     * Base constructor which create {@link OkHttpClient} with {@link XWikiInterceptor} which will
     * be used in {@link Retrofit} for correct handling of requests.
     *
     * @param baseUrl Url which will be used as base for all requests in this manager. Can be:
     *                <ul>
     *                  <li>http://www.xwiki.org/xwiki/</li>
     *                  <li>http://some.site.url/xwiki/</li>
     *                  <li>http://123.231.213.132/xwiki/</li>
     *                  <li>http://123.231.213.132:123456/xwiki/</li>
     *                </ul>
     *                Strongly recommended to end url with <b>/</b> symbol, but not necessary
     */
    public BaseApiManager(String baseUrl) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(
            HttpLoggingInterceptor.Level.BODY
        );

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new XWikiInterceptor())
            .addInterceptor(loggingInterceptor)
            .build();

        // Check that url ends with `/` and put it if not
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        
        xWikiServices = initXWikiServices(retrofit);
        xWikiPhotosManager = initXWikiPhotosManager(okHttpClient, baseUrl);
    }

    /**
     * Additional constructor which will try to get base url from {@link SharedPrefsUtils} using
     * context and {@link Constants#SERVER_ADDRESS} preference name
     *
     * @param context Will be used to get info from shared preferences
     */
    public BaseApiManager(Context context) {
        this(SharedPrefsUtils.Companion.getValue(context, Constants.Companion.getSERVER_ADDRESS(), null));
    }

    /**
     * Create {@link XWikiServices} using given {@link Retrofit} instance
     *
     * @param retrofit Context for creation object
     * @return Created object
     * @see Retrofit#create(Class)
     *
     * @since 0.4
     */
    private static XWikiServices initXWikiServices(
        @NonNull Retrofit retrofit
    ) {
        return retrofit.create(XWikiServices.class);
    }

    /**
     * Create {@link XWikiPhotosManager} using given {@link OkHttpClient} client and base url
     *
     * @param client Will be used as one point of requests for result instance
     * @param baseUrl Will be used as root for all requests
     * @return Instance
     * @see XWikiPhotosManager#XWikiPhotosManager(OkHttpClient, String)
     *
     * @since 0.4
     */
    private static XWikiPhotosManager initXWikiPhotosManager(
        @NonNull OkHttpClient client,
        @NonNull String baseUrl
    ) {
        return new XWikiPhotosManager(client, baseUrl);
    }

    /**
     * @return {@link #xWikiServices}
     */
    public XWikiServices getXwikiServicesApi() {
        return xWikiServices;
    }

    /**
     * @return {@link #xWikiPhotosManager}
     *
     * @since 0.4
     */
    public XWikiPhotosManager getXWikiPhotosManager() {
        return xWikiPhotosManager;
    }
}
