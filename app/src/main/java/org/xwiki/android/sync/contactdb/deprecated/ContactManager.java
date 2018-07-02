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

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.provider.ContactsContract.Settings;
import android.util.Log;

import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.bean.XWikiUser;
import org.xwiki.android.sync.bean.XWikiUserFull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

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
@Deprecated
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
                    Log.e(TAG, "Can't synchronize users", e);
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
//                    if (localUserMaps.containsKey(xWikiUserFull.id)) {
//                        userRawId = localUserMaps.get(xWikiUserFull.id);
//                        updateContact(
//                            context,
//                            xWikiUserFull.id,
//                            xWikiUserFull.getFirstName(),
//                            xWikiUserFull.getLastName(),
//                            xWikiUserFull.getEmail(),
//                            xWikiUserFull.getPhone(),
//                            userRawId,
//                            batchOperation
//                        );
//                        localUserMaps.remove(xWikiUserFull.id);
//                    } else {
//                        addContact(
//                            context,
//                            account,
//                            xWikiUserFull,
//                            batchOperation
//                        );
//                        batchOperation.execute();// refresh for get user raw id
//                        userRawId = lookupRawContact(resolver, xWikiUserFull.id);
//                    }
//                    updateAvatar(
//                        resolver,
//                        userRawId,
//                        xWikiUserFull
//                    );
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
        AppContext.getApiManager().getXWikiPhotosManager().downloadAvatar(
            xwikiUser.getAvatar()
        ).subscribe(
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


    /**
     * Adds a single contact to the platform contacts provider.
     * This can be used to respond to a new contact found as part
     * of sync information returned from the server, or because a
     * user added a new contact.
     *
     * @param context Context to execute operations
     * @param accountName User account name to save in
     * @param user Information about contact which must be saved
     * @param batchOperation BatchOperation to add operation and execute later
     *
     * @see ContactOperations#createNewContact(Context, String, String, boolean, BatchOperation)
     * @see ContactOperations#addName(String, String, String)
     *
     * @since 0.4
     */
    private static void addContact(
        Context context,
        String accountName,
        XWikiUserFull user,
        BatchOperation batchOperation
    ) {

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
     * Updates a single contact to the platform contacts provider.
     * <p>
     * This operation is actually relatively complex.  We query
     * the database to find all the rows of info that already
     * exist for this Contact. For rows that exist (and thus we're
     * modifying existing fields), we create an update operation
     * to change that field.  But for fields we're adding, we create
     * "add" operations to create new rows for those fields.
     *
     * @param context Context to execute operations
     * @param id Id of contact
     * @param firstName First name of contact
     * @param lastName Last name of contact
     * @param email Email of contact
     * @param phone Phone of contact
     * @param rawContactId   the unique Id for this user in contacts provider
     * @param batchOperation BatchOperation to add operation and execute later
     *
     * @since 0.4
     */
    public static void updateContact(
        Context context,
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        long rawContactId,
        BatchOperation batchOperation
    ) {

        ContentResolver resolver = context.getContentResolver();

        boolean existingCellPhone = false;
        boolean existingHomePhone = false;
        boolean existingWorkPhone = false;
        boolean existingEmail = false;

        final Cursor c = resolver.query(
            DataQuery.CONTENT_URI,
            DataQuery.PROJECTION,
            DataQuery.SELECTION,
            new String[] {
                String.valueOf(
                    rawContactId
                )
            },
            null
        );
        final ContactOperations contactOp = ContactOperations.updateExistingContact(
            context,
            rawContactId,
            true,
            batchOperation
        );

        try {
            while (c.moveToNext()) {
                final long rawId = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, rawId);
                switch (mimeType) {
                    case StructuredName.CONTENT_ITEM_TYPE:
                        contactOp.updateName(
                            uri,
                            c.getString(DataQuery.COLUMN_GIVEN_NAME),
                            c.getString(DataQuery.COLUMN_FAMILY_NAME),
                            c.getString(DataQuery.COLUMN_FULL_NAME),
                            firstName,
                            lastName,
                            null
                        );
                        break;
                    case Phone.CONTENT_ITEM_TYPE: {
                        switch (c.getInt(DataQuery.COLUMN_PHONE_TYPE)) {
                            case Phone.TYPE_MOBILE:
                                existingCellPhone = true;
                                contactOp.updatePhone(
                                    c.getString(
                                        DataQuery.COLUMN_PHONE_NUMBER
                                    ),
                                    phone,
                                    uri
                                );
                            break;
                            case Phone.TYPE_HOME:
                                existingHomePhone = true;
                                contactOp.updatePhone(
                                    c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                    phone,
                                    uri
                                );
                            break;
                            case Phone.TYPE_WORK:
                                existingWorkPhone = true;
                                contactOp.updatePhone(
                                    c.getString(
                                        DataQuery.COLUMN_PHONE_NUMBER
                                    ),
                                    phone,
                                    uri
                                );
                            break;
                        }
                        break;
                    }
                    case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE: {
                        existingEmail = true;
                        int type = c.getInt(DataQuery.COLUMN_EMAIL_TYPE);
                        if (type == ContactsContract.CommonDataKinds.Email.TYPE_WORK) {
                            contactOp.updateEmail(
                                email,
                                c.getString(DataQuery.COLUMN_EMAIL_ADDRESS),
                                uri
                            );
                        }
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }

        // Add the cell phone, if present and not updated above
        if (!existingCellPhone) {
            contactOp.addPhone(phone, Phone.TYPE_MOBILE);
        }
        // Add the home phone, if present and not updated above
        if (!existingHomePhone) {
            contactOp.addPhone(phone, Phone.TYPE_HOME);
        }

        // Add the work phone, if present and not updated above
        if (!existingWorkPhone) {
            contactOp.addPhone(phone, Phone.TYPE_WORK);
        }
        // Add the email address, if present and not updated above
        if (!existingEmail) {
            contactOp.addEmail(email);
        }

        final long profileId = lookupProfile(resolver, id);
        if (profileId <= 0) {
            contactOp.addProfileAction(id);
        }
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
     * Return a User object with data extracted from a contact stored
     * in the local contacts database.
     * <p>
     * Because a contact is actually stored over several rows in the
     * database, our query will return those multiple rows of information.
     * We then iterate over the rows and build the User structure from
     * what we find.
     *
     * @param resolver Resolver for finding
     * @param rawContactId the unique ID for the local contact
     * @return a User object containing info on that contact
     */
    public static XWikiUser getXWikiUser(ContentResolver resolver, long rawContactId) {
        String firstName = null;
        String lastName = null;
        String cellPhone = null;
        String email = null;
        String serverId = null;

        final Cursor c =
                resolver.query(
                    DataQuery.CONTENT_URI,
                    DataQuery.PROJECTION,
                    DataQuery.SELECTION,
                    new String[]{
                        String.valueOf(rawContactId)
                    },
                    null
                );
        try {
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final String tempServerId = c.getString(DataQuery.COLUMN_SERVER_ID);
                if (tempServerId != null) {
                    serverId = tempServerId;
                }
                final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
                switch (mimeType) {
                    case StructuredName.CONTENT_ITEM_TYPE:
                        lastName = c.getString(DataQuery.COLUMN_FAMILY_NAME);
                        firstName = c.getString(DataQuery.COLUMN_GIVEN_NAME);
                        break;
                    case Phone.CONTENT_ITEM_TYPE:
                        final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                        if (type == Phone.TYPE_MOBILE) {
                            cellPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                        }
                        break;
                    case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                        email = c.getString(DataQuery.COLUMN_EMAIL_ADDRESS);
                        break;
                }
            } // while
        } finally {
            c.close();
        }

        // Now that we've extracted all the information we care about,
        // create the actual User object.
        return new XWikiUser(
            serverId,
            null,
            firstName,
            lastName,
            email,
            cellPhone,
            null,
            rawContactId,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }


    /**
     * When we first add a sync adapter to the system, the contacts from that
     * sync adapter will be hidden unless they're merged/grouped with an existing
     * contact.  But typically we want to actually show those contacts, so we
     * need to mess with the Settings table to get them to show up.
     *
     * @param resolver Will need to insert new data into system db
     * @param account the Account who's visibility we're changing
     * @param visible true if we want the contacts visible, false for hidden
     *
     * @since 0.4
     */
    public static void setAccountContactsVisibility(
        ContentResolver resolver,
        Account account,
        boolean visible
    ) {
        ContentValues values = new ContentValues();
        values.put(RawContacts.ACCOUNT_NAME, account.name);
        values.put(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        values.put(Settings.UNGROUPED_VISIBLE, visible ? 1 : 0);

        resolver.insert(Settings.CONTENT_URI, values);
    }

    /**
     * Deletes a contact from the platform contacts provider. This method is used
     * both for contacts that were deleted locally and then that deletion was synced
     * to the server, and for contacts that were deleted on the server and the
     * deletion was synced to the client.
     *
     * @param rawContactId the unique Id for this rawContact in contacts
     *                     provider
     * @param batchOperation Operations holder to insert delete operation
     *
     * @since 0.4
     */
    private static void deleteContact(long rawContactId, BatchOperation batchOperation) {
        batchOperation.add(
            ContactOperations.newDeleteCpo(
                ContentUris.withAppendedId(
                    RawContacts.CONTENT_URI,
                    rawContactId
                ),
                true,
                true
            ).build()
        );
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
    public static long lookupRawContact(ContentResolver resolver, String serverContactId) {
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
     * Returns the Data id for a sample SyncAdapter contact's profile row, or 0
     * if the sample SyncAdapter user isn't found.
     *
     * @param resolver a content resolver
     * @param userId   the sample SyncAdapter user ID to lookup
     * @return the profile Data row id, or 0 if not found
     */
    private static long lookupProfile(ContentResolver resolver, String userId) {
        long profileId = 0;
        final Cursor c =
                resolver.query(
                    Data.CONTENT_URI,
                    ProfileQuery.PROJECTION,
                    ProfileQuery.SELECTION,
                    new String[]{
                        String.valueOf(userId)
                    },
                    null
                );
        try {
            if ((c != null) && c.moveToFirst()) {
                profileId = c.getLong(ProfileQuery.COLUMN_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return profileId;
    }

    //TODO:: Rewrite usage of next classes to classes which will do their responsibilities

    /**
     * Help class which contains constants which can be used for getting profile info
     *
     * @since 0.3.0
     */
    private final static class ProfileQuery {

        /**
         * Nobody must not create instance of this class
         */
        private ProfileQuery() {}

        /**
         * Projection of columns to use in
         * {@link ContentResolver#query(Uri, String[], Bundle, CancellationSignal)}
         *
         * @see ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
         * @see ContentResolver#query(Uri, String[], String, String[], String)
         * @see ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
         */
        final static String[] PROJECTION = new String[] {
            Data._ID
        };

        /**
         * Index of identifier column
         */
        final static int COLUMN_ID = 0;

        /**
         * Selection for getting profile by default params:
         * {@link ContactColumns#MIME_PROFILE} as mime and user identifier
         */
        final static String SELECTION =
            Data.MIMETYPE + "='"
            + ContactColumns.MIME_PROFILE + "' AND "
            + ContactColumns.DATA_PID + "=?";
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
     * Constants for a query to get contact data for a given rawContactId
     *
     * @since 0.3.0
     */
    final private static class DataQuery {

        /**
         * Nobody must not create instance of this class
         */
        private DataQuery() {}

        /**
         * Projection of columns to use in
         * {@link ContentResolver#query(Uri, String[], Bundle, CancellationSignal)}
         *
         * @see ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
         * @see ContentResolver#query(Uri, String[], String, String[], String)
         * @see ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
         */
        static final String[] PROJECTION = new String[]{
            Data._ID,
            RawContacts.SOURCE_ID,
            Data.MIMETYPE,
            Data.DATA1,
            Data.DATA2,
            Data.DATA3,
            Data.DATA15,
            Data.SYNC1
        };

        /**
         * Index of id of query result column
         */
        static final int COLUMN_ID = 0;

        /**
         * Index of id of server column
         */
        static final int COLUMN_SERVER_ID = 1;

        /**
         * Index of mimetype column
         */
        static final int COLUMN_MIMETYPE = 2;

        /**
         * Will be used in different places
         *
         * @see #COLUMN_PHONE_NUMBER
         * @see #COLUMN_EMAIL_ADDRESS
         * @see #COLUMN_FULL_NAME
         */
        static final int COLUMN_DATA1 = 3;

        /**
         * Will be used in different places
         *
         * @see #COLUMN_PHONE_TYPE
         * @see #COLUMN_EMAIL_TYPE
         * @see #COLUMN_GIVEN_NAME
         */
        static final int COLUMN_DATA2 = 4;

        /**
         * Will be used in different places
         *
         * @see #COLUMN_FAMILY_NAME
         */
        static final int COLUMN_DATA3 = 5;

        /**
         * Will be used to set uri path in
         * {@link ContentResolver#query(Uri, String[], Bundle, CancellationSignal)} and similar
         *
         * @see ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
         * @see ContentResolver#query(Uri, String[], String, String[], String)
         * @see ContentResolver#query(Uri, String[], String, String[], String, CancellationSignal)
         */
        static final Uri CONTENT_URI = Data.CONTENT_URI;

        /**
         * Index of phone number column
         */
        static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;

        /**
         * Index of phone type column
         */
        static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;

        /**
         * Index of email address column
         */
        static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;

        /**
         * Index of email type column
         */
        static final int COLUMN_EMAIL_TYPE = COLUMN_DATA2;

        /**
         * Index of full name column
         */
        static final int COLUMN_FULL_NAME = COLUMN_DATA1;

        /**
         * Index of given name column
         */
        static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;

        /**
         * Index of family name column
         */
        static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;

        /**
         * Selection for getting data by row id
         */
        static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
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
