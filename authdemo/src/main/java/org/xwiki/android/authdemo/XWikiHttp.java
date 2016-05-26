package org.xwiki.android.authdemo;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * XWikiHttp.
 */
public class XWikiHttp {

    public static void isValidToken(String server, String authToken, Callback callback) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = "http://"+server+"/xwiki/bin/login/XWiki/XWikiLogin";
        Request request = new Request.Builder()
                .addHeader("Cookie", authToken)
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
