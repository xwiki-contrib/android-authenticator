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
package org.xwiki.android.sync.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its
 * IBinder.
 *
 * @version $Id$
 */
public class SyncService extends Service {

    /**
     * Object which will be used for synchronized process
     */
    private static final Object sSyncAdapterLock = new Object();

    /**
     * Instance of sync adapter
     */
    private static SyncAdapter sSyncAdapter = null;

    /**
     * Init {@link #sSyncAdapter} if it is null.
     */
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * @return {@link SyncAdapter#getSyncAdapterBinder()} of {@link #sSyncAdapter}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
