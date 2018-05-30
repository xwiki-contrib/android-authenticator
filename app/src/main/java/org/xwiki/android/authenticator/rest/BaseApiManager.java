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
package org.xwiki.android.authenticator.rest;

import android.content.Context;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseApiManager {

    private final Retrofit retrofit;
    private final XWikiServices xWikiServices;
    private final XWikiPhotosManager xWikiPhotosManager;

    public BaseApiManager(String baseUrl) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new XWikiInterceptor())
                .addInterceptor(interceptor)
                .build();

        // Check that url ends with `/` and put it if not
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createAsync())
                .client(okHttpClient)
                .build();
        
        xWikiServices = initXWikiServices(retrofit);
        xWikiPhotosManager = initXWikiPhotosManager(okHttpClient, baseUrl);
    }
    
    public BaseApiManager(Context context) {
        this(SharedPrefsUtils.getValue(context, Constants.SERVER_ADDRESS, null));
    }
    
    private static XWikiServices initXWikiServices(
            Retrofit retrofit
    ) {
        return retrofit.create(XWikiServices.class);
    }

    private static XWikiPhotosManager initXWikiPhotosManager(
            OkHttpClient client,
            String baseUrl
    ) {
        return new XWikiPhotosManager(client, baseUrl);
    }

    public XWikiServices getXwikiServicesApi() {
        return xWikiServices;
    }

    public XWikiPhotosManager getXWikiPhotosManager() {
        return xWikiPhotosManager;
    }
}
