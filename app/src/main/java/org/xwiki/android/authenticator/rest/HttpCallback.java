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

import android.os.Handler;
import android.os.Looper;

/**
 * HttpCallback
 */
public abstract class HttpCallback {

    public Handler handler;

    public HttpCallback() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void postSuccess(final Object obj) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onSuccess(obj);
            }
        });
    }

    public void postFailure(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(msg);
            }
        });
    }

    public void onSuccess(Object obj) {
    }

    public void onFailure(String msg) {
    }

}
