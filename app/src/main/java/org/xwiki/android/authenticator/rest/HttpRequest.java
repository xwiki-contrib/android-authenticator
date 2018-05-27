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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * HttpRequest.
 */
public class HttpRequest {

    public interface HttpMethod {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
    }

    public int mMethod = 0;
    public String mUrl;
    public HttpParams httpParams;

    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
    public static int TIMEOUT = 5000;

    public HttpRequest(String url) {
        this(url, HttpMethod.GET, null);
    }

    public HttpRequest(String url, int method, HttpParams params) {
        mUrl = url;
        mMethod = method;
        if (params == null) {
            params = new HttpParams();
        }
        httpParams = params;
    }

    public int getMethod() {
        return mMethod;
    }

    public int getTIMEOUT() {
        return TIMEOUT;
    }

    public Map<String, String> getHeaders() {
        return httpParams.getHeaders();
    }

    public String getUrl() {
        //if(httpParams.getUrlParams()!=null){
        if (mMethod == HttpMethod.GET) {
            mUrl += httpParams.getUrlParams();
        }
        return mUrl;
    }

    public byte[] getBody() {
        Map<String, String> params = httpParams.getBodyParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    private byte[] encodeParameters(Map<String, String> params,
                                    String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(),
                        paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(),
                        paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset="
                + getParamsEncoding();
    }

    private String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

}
