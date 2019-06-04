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
package org.xwiki.android.sync.contactdb

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.Context
import android.content.OperationApplicationException
import android.net.Uri
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log

import java.util.ArrayList

/**
 * This class handles execution of batch mOperations on Contacts provider.
 *
 * @version $Id: ffaa4c03825a7f134d5e9af8af51190d7065a2d5 $
 */
class BatchOperation
/**
 * Standard constructor.
 *
 * @param resolver Will be set to [.mResolver]
 */
(
        /**
         * Variable for executing operations.
         */
        private val mResolver: ContentResolver) {

    /**
     * Currently actual operations list.
     */
    private val mOperations: ArrayList<ContentProviderOperation>

    init {
        mOperations = ArrayList()
    }

    /**
     * @return Currently not executed operations
     */
    @Synchronized
    fun size(): Int {
        return mOperations.size
    }

    /**
     * Add operation to the list for future executing.
     *
     * @param cpo Operation for adding
     */
    @Synchronized
    fun add(cpo: ContentProviderOperation) {
        mOperations.add(cpo)
    }

    /**
     * Execute operations which are stored in [.mOperations].
     *
     * @return Result of executing [Uri]'s list
     */
    @Synchronized
    fun execute(): List<Uri> {
        val resultUris = ArrayList<Uri>()

        if (mOperations.size == 0) {
            return resultUris
        }

        try {
            val results = mResolver.applyBatch(
                    ContactsContract.AUTHORITY,
                    mOperations
            )
            if (results.size > 0) {
                for (result in results) {
                    resultUris.add(result.uri)
                }
            }
        } catch (e1: OperationApplicationException) {
            Log.e(TAG, "storing contact data failed", e1)
        } catch (e2: RemoteException) {
            Log.e(TAG, "storing contact data failed", e2)
        }

        mOperations.clear()
        return resultUris
    }

    companion object {

        /**
         * Tag for logging.
         */
        private val TAG = "BatchOperation"
    }
}
