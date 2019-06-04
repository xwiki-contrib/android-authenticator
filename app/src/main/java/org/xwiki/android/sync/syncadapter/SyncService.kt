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
package org.xwiki.android.sync.syncadapter

import android.app.Service
import android.content.Intent
import android.os.IBinder


/**
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its
 * IBinder.
 *
 * @version $Id: 291666b3aeb724f517ab554e331df8a0962b9e09 $
 */
class SyncService : Service() {

    /**
     * Init [.sSyncAdapter] if it is null.
     */
    override fun onCreate() {
        synchronized(sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = SyncAdapter(applicationContext, true)
            }
        }
    }

    /**
     * @return [SyncAdapter.getSyncAdapterBinder] of [.sSyncAdapter]
     */
    override fun onBind(intent: Intent): IBinder? {
        return sSyncAdapter!!.syncAdapterBinder
    }

    companion object {

        /**
         * Object which will be used for synchronized process
         */
        private val sSyncAdapterLock = Any()

        /**
         * Instance of sync adapter
         */
        private var sSyncAdapter: SyncAdapter? = null
    }
}
