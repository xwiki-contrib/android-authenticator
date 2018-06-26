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
package org.xwiki.android.sync.utils;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Utils for simple work with intents
 *
 * @version $Id$
 */
public class IntentUtils {

    /**
     * Check url (warning, url must start with "https://" or other protocol if you want not use
     * http) and open browser or other application which can open that url.
     *
     * @param url Url to prepare
     * @return NOT STARTED, but prepared intent
     */
    public static Intent openLink(String url) {
        // if protocol isn't defined use http by default
        if (!TextUtils.isEmpty(url) && !url.contains("://")) {
            url = "http://" + url;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }
}
