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


import android.text.TextUtils;

import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

public class XWikiInterceptor implements Interceptor {

    public static final String HEADER_CONTENT_TYPE = "Content-type";
    public static final String HEADER_ACCEPT = "Accept";

    public String cookie;

    public XWikiInterceptor() {
        cookie = SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(),
                Constants.COOKIE, "");
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request chainRequest = chain.request();

        HttpUrl originalHttpUrl = chainRequest.url();
        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("media", "json")
                .build();

        Builder builder = chainRequest.newBuilder()
                .header(HEADER_CONTENT_TYPE, "application/json")
                .header(HEADER_ACCEPT, "application/json")
                .url(url);

        if (!TextUtils.isEmpty(cookie)) {
            builder.addHeader("Cookie", cookie);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
