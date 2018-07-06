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
package org.xwiki.android.sync.contactdb;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.bean.XWikiUser;
import org.xwiki.android.sync.bean.XWikiUserFull;
import org.xwiki.android.sync.contactdb.BatchOperation;
import org.xwiki.android.sync.rest.XWikiHttp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import retrofit2.HttpException;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.xwiki.android.sync.contactdb.ContactOperationsKt.toContentProviderOperations;

/**
 * Class for managing contacts sync related mOperations.
 *
 * @version $Id$
 */
public class ContactManager {
    private static final String TAG = "ContactManager";

    /**
     * Subscribe to observable to get {@link XWikiUserFull} objects and save locally.
     *
     * @param context The context of Authenticator Activity
     * @param account The username for the account
     * @param observable Will be used to subscribe to get stream of users
     *
     * @since 0.4
     */
    public static synchronized void updateContacts(
        final Context context,
        final String account,
        final Observable<XWikiUserFull> observable
    ) {
        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(resolver);
        final HashMap<String, Long> localUserMaps = getAllContactsIdMap(resolver, account);

        observable.subscribeOn(
            Schedulers.newThread()
        ).subscribe(
            new Observer<XWikiUserFull>() {
                @Override
                public void onCompleted() {
                    for (String id : localUserMaps.keySet()) {
                        long rawId = localUserMaps.get(id);
                        if (batchOperation.size() >= 100) {
                            batchOperation.execute();
                        }
                    }
                    batchOperation.execute();
                }

                @Override
                public void onError(Throwable e) {
                    try {
                        HttpException asHttpException = (HttpException) e;
                        if (asHttpException.code() == 401) {//Unauthorized
                            XWikiHttp.relogin(
                                context,
                                account
                            );
                        }
                    } catch (ClassCastException e1) {
                        Log.e(TAG, "Can't synchronize users", e);
                    }
                }

                @Override
                public void onNext(XWikiUserFull xWikiUserFull) {
                    List<ContentProviderOperation> operationList = toContentProviderOperations(
                        xWikiUserFull,
                        resolver,
                        account
                    );
                    for (ContentProviderOperation operation : operationList) {
                        batchOperation.add(operation);
                    }
                    if (batchOperation.size() >= 100) {
                        batchOperation.execute();
                    }
                    updateAvatar(
                        resolver,
                        lookupRawContact(resolver, xWikiUserFull.id),
                        xWikiUserFull
                    );
                }
            }
        );
    }


    /**
     * Initiate procedure of contact avatar updating
     *
     * @param contentResolver Resolver to get contact photo file
     * @param rawId User row id in local store
     * @param xwikiUser Xwiki user info to find avatar
     *
     * @see #writeDisplayPhoto(ContentResolver, long, byte[])
     *
     * @since 0.4
     */
    public static void updateAvatar(
        final ContentResolver contentResolver,
        final long rawId,
        XWikiUserFull xwikiUser
    ) {
        Observable<byte[]> gettingAvatarObservable = AppContext
            .getApiManager()
            .getXWikiPhotosManager()
            .downloadAvatar(
                xwikiUser.pageName,
                xwikiUser.getAvatar()
            );
        if (gettingAvatarObservable != null) {
            gettingAvatarObservable.subscribe(
                new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {
                        if (bytes != null) {
                            try {
                                writeDisplayPhoto(contentResolver, rawId, bytes);
                            } catch (IOException e) {
                                Log.e(
                                    TAG,
                                    "Can't update avatar of user",
                                    e
                                );
                            }
                        }
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(
                            TAG,
                            "Can't update avatar of user",
                            throwable
                        );
                    }
                }
            );
        }
    }


    /**
     * Write photo of contact from bytes to file
     *
     * @param contentResolver Will be used to get file descriptor of contact
     * @param rawContactId Contact id
     * @param photo Photo bytes which can be get from server
     *
     * @see ContentResolver#openAssetFileDescriptor(Uri, String)
     * @see Uri#withAppendedPath(Uri, String)
     *
     * @since 0.4
     */
    private static void writeDisplayPhoto(
        ContentResolver contentResolver,
        long rawContactId,
        byte[] photo
    ) throws IOException {
        Uri rawContactPhotoUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(
                    RawContacts.CONTENT_URI,
                    rawContactId
                ),
                RawContacts.DisplayPhoto.CONTENT_DIRECTORY
        );
        AssetFileDescriptor fd = contentResolver.openAssetFileDescriptor(rawContactPhotoUri, "rw");
        OutputStream os = fd.createOutputStream();
        os.write(photo);
        os.close();
        fd.close();
    }

    /**
     * Read database and get maps with id's.
     *
     * @param resolver Resolver to find users
     * @param accountName Account to get data from resolver
     * @return Map with pairs <b>server_id</b> to <b>user_id</b>
     *
     * @since 0.4
     */
    private static HashMap<String, Long> getAllContactsIdMap(
        ContentResolver resolver,
        String accountName
    ) {
        HashMap<String, Long> allMaps = new HashMap<>();
        final Cursor c = resolver.query(
            AllQuery.CONTENT_URI,
            AllQuery.PROJECTION,
            AllQuery.SELECTION,
            new String[] {
                accountName
            },
            null
        );
        try {
            while (c.moveToNext()) {
                final String serverId = c.getString(AllQuery.COLUMN_SERVER_ID);

                final long rawId = c.getLong(AllQuery.COLUMN_RAW_CONTACT_ID);

                allMaps.put(serverId, rawId);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return allMaps;
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
    private static long lookupRawContact(ContentResolver resolver, String serverContactId) {
        long rawContactId = 0;
        final Cursor c = resolver.query(
                UserIdQuery.CONTENT_URI,
                UserIdQuery.PROJECTION,
                UserIdQuery.SELECTION,
                new String[] {
                    String.valueOf(serverContactId)
                },
                null);
        try {
            if ((c != null) && c.moveToFirst()) {
                rawContactId = c.getLong(UserIdQuery.COLUMN_RAW_CONTACT_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return rawContactId;
    }

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     *
     * @since 0.3.0
     */
    final private static class UserIdQuery {

        /**
         * Nobody must not create instance of this class
         */
        private UserIdQuery() {}

        /**
         * Projection of columns to use in
         * {@link ContentResolver#query(Uri, String[], Bundle, CancellationSignal)}
         *
         * @see ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
         * @see ContentResolver#query(Uri, String[], String, String[], String)
         * @see ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
         */
        final static String[] PROJECTION = new String[]{
                RawContacts._ID,
                RawContacts.CONTACT_ID
        };

        /**
         * Column which contains user raw id
         */
        final static int COLUMN_RAW_CONTACT_ID = 0;

        /**
         * Will be used to set uri path in
         * {@link ContentResolver#query(Uri, String[], Bundle, CancellationSignal)} and similar
         *
         * @see ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
         * @see ContentResolver#query(Uri, String[], String, String[], String)
         * @see ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
         */
        final static Uri CONTENT_URI = RawContacts.CONTENT_URI;

        /**
         * Selection for getting user by type {@link Constants#ACCOUNT_TYPE} and source id
         */
        static final String SELECTION =
            RawContacts.ACCOUNT_TYPE + "='"
            + Constants.ACCOUNT_TYPE + "' AND "
            + RawContacts.SOURCE_ID + "=?";
    }

    /**
     * Getting all rows id's helper class
     *
     * @since 0.3.0
     */
    final private static class AllQuery {

        /**
         * Nobody must not create instance of this class
         */
        private AllQuery() {}

        /**
         * Projection of columns to use in
         * {@link ContentResolver#query(Uri, String[], Bundle, CancellationSignal)}
         *
         * @see ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
         * @see ContentResolver#query(Uri, String[], String, String[], String)
         * @see ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
         */
        final static String[] PROJECTION = new String[]{
                RawContacts._ID,
                RawContacts.SOURCE_ID,
        };

        /**
         * Column which contains raw id
         */
        final static int COLUMN_RAW_CONTACT_ID = 0;

        /**
         * Column which contains server id
         */
        final static int COLUMN_SERVER_ID = 1;

        /**
         * Will be used to set uri path in
         * {@link ContentResolver#query(Uri, String[], Bundle, CancellationSignal)} and similar
         *
         * @see ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
         * @see ContentResolver#query(Uri, String[], String, String[], String)
         * @see ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
         */
        static final Uri CONTENT_URI =
            RawContacts.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(
                    ContactsContract.CALLER_IS_SYNCADAPTER,
                    "true"
                )
                .build();

        /**
         * Selection for getting data by {@link Constants#ACCOUNT_TYPE} and account name
         */
        static final String SELECTION =
            RawContacts.ACCOUNT_TYPE + "='"
            + Constants.ACCOUNT_TYPE + "' AND "
            + RawContacts.ACCOUNT_NAME + "=?";
    }
}
