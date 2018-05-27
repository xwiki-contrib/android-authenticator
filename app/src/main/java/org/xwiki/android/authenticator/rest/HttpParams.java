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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HttpParams
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

    public void putBodyParams(final String key, final int value) {
        mBodyParams.put(key, value + "");
    }

    public void putBodyParams(final String key, final String value) {
        mBodyParams.put(key, value);
    }

    public void putUrlParams(final String key, final int value) {
        urlParams.put(key, value + "");
    }

    public void putUrlParams(final String key, final String value) {
        urlParams.put(key, value);
    }

}
