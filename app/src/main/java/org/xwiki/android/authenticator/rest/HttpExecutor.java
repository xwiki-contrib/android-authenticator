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

import android.util.Log;

import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;

/**
 * HttpExecutor
 * performRequest is the api to execute the http request.
 */
public class HttpExecutor {
    private static final String TAG = "HttpExecutor";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private final SSLSocketFactory mSslSocketFactory;
    private static String COOKIE = null;

    public HttpExecutor() {
        mSslSocketFactory = null;
    }

    public HttpResponse performRequest(HttpRequest request) throws IOException {
        //request url
        String url = request.getUrl();
        //request headers
        HashMap<String, String> map = new HashMap<>();
        map.putAll(request.getHeaders());
        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);
        for (String headerName : map.keySet()) {
            connection.addRequestProperty(headerName, map.get(headerName));
        }
        COOKIE = SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, null);
        if (COOKIE != null && COOKIE.length() > 0) {
            connection.addRequestProperty("Cookie", COOKIE);
            //connection.setRequestProperty("Cookie", COOKIE);
        }
        Log.d(TAG, "url=" + url + ", header=" + map.toString() + ", Cookie=" + (COOKIE != null ? COOKIE : ""));
        setConnectionParametersForRequest(connection, request);
        HttpResponse response = responseFromConnection(connection);
        Log.d(TAG, "response: code=" + response.getResponseCode() + response.getResponseMessage() + ", header=" + response.getHeaders().toString());
        return response;
    }

    private HttpURLConnection openConnection(URL url, HttpRequest request)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int timeoutMs = request.getTIMEOUT();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        // https://developer.android.com/training/articles/security-ssl.html#SelfSigned
        // HttpUrlConnection provides default support for ssl if the ca is issued by a well known CA.
        // but if it's self signed, you should trust all or Create a KeyStore containing your trusted CAs.
        // for HTTPS
        /*
        if ("https".equals(url.getProtocol())) {
            if (mSslSocketFactory != null) {
                //use caller-provided custom SslSocketFactory
                ((HttpsURLConnection) connection)
                        .setSSLSocketFactory(mSslSocketFactory);
            } else {
                //trust all
                HTTPSTrustManager.allowAllSSL();
            }
        }
        */
        return connection;
    }

    /**
     * isSelfSigned
     * check if the server ca is self signed
     * but this function is just for test, because it's not secure
     * we can just choose one type according to our server(trust well known ca or create our own trust keystore)
     * https://developer.android.com/training/articles/security-ssl.html#SelfSigned
     * @param url
     * @return
     */
    private boolean isSelfSigned(URL url){
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
        } catch (SSLHandshakeException e){
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setConnectionParametersForRequest(HttpURLConnection urlConnection, HttpRequest request) throws IOException {
        switch (request.getMethod()) {
            case HttpRequest.HttpMethod.GET:
                urlConnection.setRequestMethod("GET");
                break;
            case HttpRequest.HttpMethod.POST:
                urlConnection.setRequestMethod("POST");
                addBodyIfExists(urlConnection, request);
                break;
            case HttpRequest.HttpMethod.PUT:
                urlConnection.setRequestMethod("PUT");
                addBodyIfExists(urlConnection, request);
                break;
            case HttpRequest.HttpMethod.DELETE:
                urlConnection.setRequestMethod("DELETE");
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection,
                                        HttpRequest request) throws IOException {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty(HEADER_CONTENT_TYPE,
                    request.getBodyContentType());
            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }


    private HttpResponse responseFromConnection(HttpURLConnection connection) throws IOException {
        HttpResponse response = new HttpResponse();
        //contentStream
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            throw new IOException(
                    "Could not retrieve response code from HttpUrlConnection.");
        }
        response.setResponseCode(responseCode);
        response.setResponseMessage(connection.getResponseMessage());
        //response.setContentStream(inputStream);
        response.setContentData(getByteArrayFromInputStream(inputStream));
        response.setContentLength(connection.getContentLength());
        response.setContentEncoding(connection.getContentEncoding());
        response.setContentType(connection.getContentType());
        //header
        Map<String, String> headerMap = new HashMap<>();
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields()
                .entrySet()) {
            if (header.getKey() != null) {
                String value = "";
                for (String v : header.getValue()) {
                    value += (v + "; ");
                }
                headerMap.put(header.getKey(), value);
            }
        }
        response.setHeaders(headerMap);
        return response;
    }

    /**
     * getByteArrayFromInputStream
     * close the network inputStream, and transfer it to byte array stored in HttpResponse.
     * if needed the inputStream, just use the new ByteArrayInputStream.
     *
     * @param is
     * @return
     * @throws IOException
     */
    private byte[] getByteArrayFromInputStream(InputStream is)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        byte[] bytes = os.toByteArray();
        os.close();
        return bytes;
    }

}
