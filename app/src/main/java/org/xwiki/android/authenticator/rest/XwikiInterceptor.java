
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

public class XwikiInterceptor implements Interceptor {

    public static final String HEADER_CONTENT_TYPE = "Content-type";
    public static final String HEADER_ACCEPT = "Accept";

    public String cookie;

    public XwikiInterceptor() {
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
