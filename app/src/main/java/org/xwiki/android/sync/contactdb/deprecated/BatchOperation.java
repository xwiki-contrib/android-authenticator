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
package org.xwiki.android.sync.contactdb.deprecated;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles execution of batch mOperations on Contacts provider.
 *
 * @version $Id$
 */
@Deprecated
final public class BatchOperation {

    /**
     * Tag for logging.
     */
    private static final String TAG = "BatchOperation";

    /**
     * Variable for executing operations.
     */
    private final ContentResolver mResolver;

    /**
     * Currently actual operations list.
     */
    private final ArrayList<ContentProviderOperation> mOperations;

    /**
     * Standard constructor.
     *
     * @param resolver Will be set to {@link #mResolver}
     */
    public BatchOperation(ContentResolver resolver) {
        mResolver = resolver;
        mOperations = new ArrayList<>();
    }

    /**
     * @return Currently not executed operations
     */
    public synchronized int size() {
        return mOperations.size();
    }

    /**
     * Add operation to the list for future executing.
     *
     * @param cpo Operation for adding
     */
    public synchronized void add(ContentProviderOperation cpo) {
        mOperations.add(cpo);
    }

    /**
     * Execute operations which are stored in {@link #mOperations}.
     *
     * @return Result of executing {@link Uri}'s list
     */
    public synchronized List<Uri> execute() {
        List<Uri> resultUris = new ArrayList<>();

        if (mOperations.size() == 0) {
            return resultUris;
        }

        try {
            ContentProviderResult[] results = mResolver.applyBatch(
                ContactsContract.AUTHORITY,
                mOperations
            );
            if (results.length > 0) {
                for (ContentProviderResult result : results) {
                    resultUris.add(result.uri);
                }
            }
        } catch (final OperationApplicationException e1) {
            Log.e(TAG, "storing contact data failed", e1);
        } catch (final RemoteException e2) {
            Log.e(TAG, "storing contact data failed", e2);
        }

        mOperations.clear();
        return resultUris;
    }
}
