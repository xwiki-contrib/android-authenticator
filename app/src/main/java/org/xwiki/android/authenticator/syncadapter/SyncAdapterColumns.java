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
package org.xwiki.android.authenticator.syncadapter;

import android.provider.ContactsContract.Data;

/*
 * The standard columns representing contact's info from social apps.
 */
public final class SyncAdapterColumns {

    private SyncAdapterColumns() {
    }

    /**
     * MIME-type used when storing a profile {@link android.provider.ContactsContract.Data} entry.
     */
    public static final String MIME_PROFILE = "vnd.android.cursor.item/vnd.xwikiauth.profile";

    public static final String DATA_PID = Data.DATA1;

    public static final String DATA_SUMMARY = Data.DATA2;

    public static final String DATA_DETAIL = Data.DATA3;
}
