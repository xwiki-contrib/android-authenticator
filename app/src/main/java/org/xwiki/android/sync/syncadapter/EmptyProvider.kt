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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Define an empty implementation of ContentProvider.
 * If the sync adapter framework tries to run your sync adapter,
 * and your app doesn't have a content provider, your sync adapter crashes.
 * reference: http://developer.android.com/training/sync-adapters/creating-stub-provider.html .
 *
 *
 *
 * In fact this provider doing nothing.
 *
 * @version $Id: 2b5aa90b98bde32af5f432238154ba15ea56521d $
 */
class EmptyProvider : ContentProvider() {

    /**
     * Always return true, indicating that the provider loaded correctly.
     */
    override fun onCreate(): Boolean {
        return true
    }

    /**
     * Always returns no results
     */
    override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    /**
     * Always returns null (no URI)
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    /**
     * Always returns "no rows affected" (0)
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    /**
     * Always returns "no rows affected" (0)
     */
    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<String>?): Int {
        return 0
    }
}
