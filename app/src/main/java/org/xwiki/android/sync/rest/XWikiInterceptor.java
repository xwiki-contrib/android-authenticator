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

import android.text.TextUtils;

import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

/**
 * Must be used for each {@link okhttp3.OkHttpClient} which you will create in
 * {@link BaseApiManager} bounds.
 *
 * @version $Id$
 */
public class XWikiInterceptor implements Interceptor {

    private static final String HEADER_CONTENT_TYPE = "Content-type";
    private static final String HEADER_ACCEPT = "Accept";

    private static final String HEADER_COOKIE = "Cookie";

    private static final String CONTENT_TYPE = "application/json";

    /**
     * Contains cached coolie value.
     */
    private String cookie = null;

    /**
     * Add query parameter <b>media=json</b>, headers {@link #HEADER_ACCEPT}={@link #CONTENT_TYPE}
     * and {@link #HEADER_CONTENT_TYPE}={@link #CONTENT_TYPE}, also of {@link #getCookie()} will
     * not be null - header {@link #HEADER_COOKIE} will be added with value from
     * {@link #getCookie()}.
     *
     * @param chain
     * @return Response from server with parameters
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request chainRequest = chain.request();

        HttpUrl originalHttpUrl = chainRequest.url();
        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("media", "json")
                .build();

        Builder builder = chainRequest.newBuilder()
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                .header(HEADER_ACCEPT, CONTENT_TYPE)
                .url(url);

        String cookie = getCookie();

        if (!TextUtils.isEmpty(cookie)) {
            builder.addHeader(HEADER_COOKIE, cookie);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }

    /**
     * If {@link #cookie} is null or empty will check shared preferences
     * and if cookies available - set {@link #cookie}
     *
     * @return {@link #cookie} after checking on not null/empty
     *
     * @since 0.4
     */
    private String getCookie() {
        if (TextUtils.isEmpty(cookie)) {
            cookie = SharedPrefsUtils.getValue(
                AppContext.getInstance().getApplicationContext(),
                Constants.COOKIE,
                ""
            );
        }
        return cookie;
    }
}
