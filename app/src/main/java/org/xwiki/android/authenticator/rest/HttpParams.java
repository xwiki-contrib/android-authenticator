package org.xwiki.android.authenticator.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fitz on 2016/4/25.
 */
public class HttpParams {
    private final Map<String, String> urlParams = new ConcurrentHashMap<String, String>(8);
    private final Map<String, String> mHeaders = new HashMap<String, String>();
    private Map<String, String> mBodyParams = new HashMap<String, String>();

    public StringBuilder getUrlParams() {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams
                .entrySet()) {
            if (!isFirst) {
                result.append("&");
            } else {
                result.append("?");
                isFirst = false;
            }
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        return result;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }
    public Map<String, String> getBodyParams() {
        return mBodyParams;
    }

    public void putHeaders(final String key, final int value) {
        this.putHeaders(key, value + "");
    }
    public void putHeaders(final String key, final String value) {
        mHeaders.put(key, value);
    }

    public void putBodyParams(final String key, final int value){
        mBodyParams.put(key, value+"");
    }
    public void putBodyParams(final String key, final String value){
        mBodyParams.put(key, value);
    }

    public void putUrlParams(final String key, final int value){
        urlParams.put(key, value+"");
    }
    public void putUrlParams(final String key, final String value){
        urlParams.put(key, value);
    }

}
