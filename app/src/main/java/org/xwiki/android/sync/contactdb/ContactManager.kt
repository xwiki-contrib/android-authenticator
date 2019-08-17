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

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import android.util.Log
import org.xwiki.android.sync.ACCOUNT_TYPE
import org.xwiki.android.sync.bean.XWikiUserFull
import org.xwiki.android.sync.resolveApiManager
import org.xwiki.android.sync.rest.BaseApiManager
import retrofit2.HttpException
import rx.Observable
import rx.Observer
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.*

/**
 * Class for managing contacts sync related mOperations.
 *
 * @version $Id: be8481217cc0fc4ab1ff4ab9e6f1b9d1fb4f5fc3 $
 */
class ContactManager(
    private val apiManager: BaseApiManager
) {
    private val TAG = "ContactManager"

    companion object {


        /**
         * Subscribe to observable to get [XWikiUserFull] objects and save locally.
         *
         * @param context The context of Authenticator Activity
         * @param account The username for the account
         * @param observable Will be used to subscribe to get stream of users
         *
         * @since 0.4
         */
        fun updateContacts(
            context: Context,
            account: UserAccount,
            observable: Observable<XWikiUserFull>
        ) {
            val apiManager = resolveApiManager(account)
            val contactManager = ContactManager(apiManager)

            val resolver = context.contentResolver
            val batchOperation = BatchOperation(resolver)
            val localUserMaps = contactManager.getAllContactsIdMap(resolver, account.accountName)

            observable.subscribe(
                object : Observer<XWikiUserFull> {
                    override fun onCompleted() {
                        for (id in localUserMaps.keys) {
                            val rawId = localUserMaps[id]
                            if (batchOperation.size() >= 100) {
                                batchOperation.execute()
                            }
                        }
                        batchOperation.execute()
                    }

                    override fun onError(e: Throwable) {
                        try {
                            val asHttpException = e as HttpException
                            if (asHttpException.code() == 401) {//Unauthorized
                                apiManager.xWikiHttp.relogin(
                                    context
                                )
                            }
                        } catch (e1: ClassCastException) {
                            Log.e(contactManager.TAG, "Can't synchronize users", e)
                        }

                    }

                    override fun onNext(xWikiUserFull: XWikiUserFull) {
                        val operationList = xWikiUserFull.toContentProviderOperations(
                            resolver,
                            account.accountName
                        )
                        for (operation in operationList) {
                            batchOperation.add(operation)
                        }
                        if (batchOperation.size() >= 100) {
                            batchOperation.execute()
                        }

                        if (!getValue(context, "data_saving", false)) {
                            contactManager.updateAvatar(
                                resolver,
                                contactManager.lookupRawContact(resolver, xWikiUserFull.id),
                                xWikiUserFull
                            )
                        }
                    }
                }
            )
        }
    }

    /**
     * Initiate procedure of contact avatar updating
     *
     * @param contentResolver Resolver to get contact photo file
     * @param rawId UserAccount row id in local store
     * @param xwikiUser Xwiki user info to find avatar
     *
     * @see .writeDisplayPhoto
     * @since 0.4
     */
    fun updateAvatar(
        contentResolver: ContentResolver,
        rawId: Long,
        xwikiUser: XWikiUserFull
    ) {
        val gettingAvatarObservable = apiManager.xWikiPhotosManager
            .downloadAvatar(
                xwikiUser.pageName,
                xwikiUser.avatar
            )
        if (gettingAvatarObservable != null) {
            gettingAvatarObservable.subscribe(
                Action1<ByteArray> { bytes ->
                    if (bytes != null) {
                        try {
                            writeDisplayPhoto(contentResolver, rawId, bytes)
                        } catch (e: IOException) {
                            Log.e(
                                TAG,
                                "Can't update avatar of user",
                                e
                            )
                        }

                    }
                },
                Action1<Throwable> { throwable ->
                    Log.e(
                        TAG,
                        "Can't update avatar of user",
                        throwable
                    )
                }
            )
        }
    }


    /**
     * Write photo of contact from bytes to file
     *
     * @param contentResolver Will be used to get file descriptor of contact
     * @param rawContactId Contact id
     * @param photo Photo bytes which can be get from server
     *
     * @see ContentResolver.openAssetFileDescriptor
     * @see Uri.withAppendedPath
     * @since 0.4
     */
    @Throws(IOException::class)
    private fun writeDisplayPhoto(
        contentResolver: ContentResolver,
        rawContactId: Long,
        photo: ByteArray?
    ) {
        val rawContactPhotoUri = Uri.withAppendedPath(
            ContentUris.withAppendedId(
                RawContacts.CONTENT_URI,
                rawContactId
            ),
            RawContacts.DisplayPhoto.CONTENT_DIRECTORY
        )
        val fd = contentResolver.openAssetFileDescriptor(rawContactPhotoUri, "rw")
        val os = fd?.createOutputStream()
        os?.write(photo)
        os?.close()
        fd?.close()
    }

    /**
     * Read database and get maps with id's.
     *
     * @param resolver Resolver to find users
     * @param accountName Account to get data from resolver
     * @return Map with pairs **server_id** to **user_id**
     *
     * @since 0.4
     */
    private fun getAllContactsIdMap(
        resolver: ContentResolver,
        accountName: String
    ): HashMap<String, Long> {
        val allMaps = HashMap<String, Long>()
        val c = resolver.query(
            AllQuery.CONTENT_URI,
            AllQuery.PROJECTION,
            AllQuery.SELECTION,
            arrayOf(accountName), null
        )
        try {
            while (c.moveToNext()) {
                val serverId = c.getString(AllQuery.COLUMN_SERVER_ID)

                val rawId = c.getLong(AllQuery.COLUMN_RAW_CONTACT_ID)

                allMaps[serverId] = rawId
            }
        } finally {
            c?.close()
        }
        return allMaps
    }

    /**
     * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
     * sample SyncAdapter user isn't found.
     *
     * @param resolver        the content resolver to use
     * @param serverContactId the sample SyncAdapter user ID to lookup
     * @return the RawContact id, or 0 if not found
     *
     * @since 0.4
     */
    private fun lookupRawContact(resolver: ContentResolver, serverContactId: String): Long {
        var rawContactId: Long = 0
        val c = resolver.query(
            UserIdQuery.CONTENT_URI,
            UserIdQuery.PROJECTION,
            UserIdQuery.SELECTION,
            arrayOf(serverContactId), null
        )
        try {
            if (c != null && c.moveToFirst()) {
                rawContactId = c.getLong(UserIdQuery.COLUMN_RAW_CONTACT_ID)
            }
        } finally {
            c?.close()
        }
        return rawContactId
    }

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     *
     * @since 0.3.0
     */
    private object UserIdQuery {

        /**
         * Projection of columns to use in
         * [ContentResolver.query]
         *
         * @see ContentResolver.query
         * @see ContentResolver.query
         * @see ContentResolver.query
         */
        internal val PROJECTION = arrayOf(RawContacts._ID, RawContacts.CONTACT_ID)

        /**
         * Column which contains user raw id
         */
        internal val COLUMN_RAW_CONTACT_ID = 0

        /**
         * Will be used to set uri path in
         * [ContentResolver.query] and similar
         *
         * @see ContentResolver.query
         * @see ContentResolver.query
         * @see ContentResolver.query
         */
        internal val CONTENT_URI = RawContacts.CONTENT_URI

        /**
         * Selection for getting user by type [Constants.ACCOUNT_TYPE] and source id
         */
        internal val SELECTION = (
                RawContacts.ACCOUNT_TYPE + "='"
                        + ACCOUNT_TYPE + "' AND "
                        + RawContacts.SOURCE_ID + "=?")
    }
    /**
     * Nobody must not create instance of this class
     */

    /**
     * Getting all rows id's helper class
     *
     * @since 0.3.0
     */
    private object AllQuery {

        /**
         * Projection of columns to use in
         * [ContentResolver.query]
         *
         * @see ContentResolver.query
         * @see ContentResolver.query
         * @see ContentResolver.query
         */
        internal val PROJECTION = arrayOf(RawContacts._ID, RawContacts.SOURCE_ID)

        /**
         * Column which contains raw id
         */
        internal val COLUMN_RAW_CONTACT_ID = 0

        /**
         * Column which contains server id
         */
        internal val COLUMN_SERVER_ID = 1

        /**
         * Will be used to set uri path in
         * [ContentResolver.query] and similar
         *
         * @see ContentResolver.query
         * @see ContentResolver.query
         * @see ContentResolver.query
         */
        internal val CONTENT_URI = RawContacts.CONTENT_URI
            .buildUpon()
            .appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER,
                "true"
            )
            .build()

        /**
         * Selection for getting data by [Constants.ACCOUNT_TYPE] and account name
         */
        internal val SELECTION = (
                RawContacts.ACCOUNT_TYPE + "='"
                        + ACCOUNT_TYPE + "' AND "
                        + RawContacts.ACCOUNT_NAME + "=?")
    }
    /**
     * Nobody must not create instance of this class
     */
}
