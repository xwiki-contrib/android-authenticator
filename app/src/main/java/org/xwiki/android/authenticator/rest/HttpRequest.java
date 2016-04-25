package org.xwiki.android.authenticator.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by fitz on 2016/4/25.
 */
public class HttpRequest {

    public interface HttpMethod{
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

    public HttpRequest(String url){
        this(url, HttpMethod.GET, null);
    }

    public HttpRequest(String url, int method, HttpParams params){
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

    public Map<String, String> getHeaders(){
        return httpParams.getHeaders();
    }

    public String getUrl(){
        //if(httpParams.getUrlParams()!=null){
        if(mMethod == HttpMethod.GET){
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
