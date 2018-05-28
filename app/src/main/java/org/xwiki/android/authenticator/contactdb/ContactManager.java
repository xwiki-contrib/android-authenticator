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
package org.xwiki.android.authenticator.contactdb;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.bean.XWikiUserFull;
import org.xwiki.android.authenticator.rest.XWikiHttp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import rx.functions.Action1;

/**
 * Class for managing contacts sync related mOperations
 */
public class ContactManager {
    private static final String TAG = "ContactManager";

    public static final String XWIKI_GROUP_NAME = "XWiki Group";

    public static long ensureXWikiGroupExists(Context context, String accountName) {
        final ContentResolver resolver = context.getContentResolver();

        // Lookup the group
        long groupId = 0;
        final Cursor cursor = resolver.query(Groups.CONTENT_URI, new String[]{Groups._ID},
                Groups.ACCOUNT_NAME + "=? AND " + Groups.ACCOUNT_TYPE + "=? AND " +
                        Groups.TITLE + "=?",
                new String[]{accountName, Constants.ACCOUNT_TYPE, XWIKI_GROUP_NAME}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    groupId = cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        if (groupId == 0) {
            // the group doesn't exist yet, so create it
            final ContentValues contentValues = new ContentValues();
            contentValues.put(Groups.ACCOUNT_NAME, accountName);
            contentValues.put(Groups.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
            contentValues.put(Groups.TITLE, XWIKI_GROUP_NAME);
            contentValues.put(Groups.GROUP_IS_READ_ONLY, true);

            final Uri newGroupUri = resolver.insert(Groups.CONTENT_URI, contentValues);
            groupId = ContentUris.parseId(newGroupUri);
        }
        return groupId;
    }


    /**
     * Take a list of updated contacts and apply those changes to the
     * contacts database. Typically this list of contacts would have been
     * returned from the server, and we want to apply those changes locally.
     *
     * @param context  The context of Authenticator Activity
     * @param account  The username for the account
     * @param syncData The list of contacts to update
     * @return the server syncState that should be used in our next
     * sync request.
     */
    public static synchronized void updateContacts(final Context context, final String account,
                                                   XWikiHttp.SyncData syncData) throws IOException, XmlPullParserException {


        // Make sure that the XWiki group exists
        //List<String> groupIdList = SharedPrefsUtil.getArrayList(AppContext.getInstance().getApplicationContext(), "SelectGroups");
        //for(String groupIdString : groupIdList){
        //    long groupId = ContactManager.ensureXWikiGroupExists(context, account);
        //}
        Log.i(TAG, "syncData updateContact size=" + syncData.getUpdateUserList().size() + ", All Id size=" + syncData.getAllIdSet().size());

        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(context, resolver);
        List<XWikiUserFull> wikiUsers = syncData.getUpdateUserList();

        // Add new contact and update changed ones
        Log.d(TAG, "Synchronizing XWiki contacts");
        if (wikiUsers != null && wikiUsers.size() > 0) {
            for (final XWikiUserFull xwikiUser : wikiUsers) {
                long rawContactId = lookupRawContact(resolver, xwikiUser.id);
                if (rawContactId != 0) {
                    Log.d(TAG, "Update contact");
                    updateContact(context, resolver, xwikiUser, false, true, true, true, rawContactId, batchOperation);
                } else {
                    Log.d(TAG, "Add contact");
                    addContact(context, account, xwikiUser, 0, true, batchOperation);
                }
                // A sync adapter should batch operations on multiple contacts,
                // because it will make a dramatic performance difference.
                // (UI updates, etc)
                if (batchOperation.size() >= 100) {
                    batchOperation.execute();
                }
            }
            batchOperation.execute();
        }

        // Remove contacts that don't exist anymore
        HashMap<String, Long> localUserMaps = getAllContactsIdMap(context, account);
        HashSet<String> allIdSet = syncData.getAllIdSet();
//        Iterator iterLocalMap = localUserMaps.entrySet().iterator();
//        while (iterLocalMap.hasNext()) {
//            HashMap.Entry entry = (HashMap.Entry) iterLocalMap.next();
//            String key = (String) entry.getKey();
//            long rawId = (Long) entry.getValue();
//            if (!allIdSet.contains(key)) {
//                deleteContact(context, rawId, batchOperation);
//                Log.d(TAG, key + " removed");
//            }else{
//                allIdSet.remove(key);
//            }
//            //avoid the exception "android.os.TransactionTooLargeException: data parcel size 1846232 bytes"
//            if (batchOperation.size() >= 100) {
//                batchOperation.execute();
//            }
//        }
//        Log.d(TAG, "Remove contacts end");
//        batchOperation.execute();

        // if allIdSet size != 0, just add these users to local database.
        // if newly adding users to the group, these following code will be execute..
        if(allIdSet.size() > 0 ) {
            final List<XWikiUserFull> userList = new ArrayList<>();
            final CountDownLatch countDown = new CountDownLatch(allIdSet.size());
            for (String item : allIdSet) {
                String[] splitted = XWikiUser.splitId(item);
                AppContext.getApiManager().getXwikiServicesApi().getFullUserDetails(
                        splitted[0],
                        splitted[1],
                        splitted[2]
                ).subscribe(
                        new Action1<XWikiUserFull>() {
                            @Override
                            public void call(XWikiUserFull xWikiUser) {
                                userList.add(xWikiUser);
                                countDown.countDown();
                                addContact(context, account, xWikiUser, 0, true, batchOperation);
                                if (batchOperation.size() >= 100) {
                                    batchOperation.execute();
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                countDown.countDown();
                            }
                        }
                );
            }
            try {
                countDown.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "Can't await update of user", e);
            }
            batchOperation.execute();
            updateAvatars(context, userList);
        }

    }


    //http://stackoverflow.com/questions/14601209/update-contact-image-in-android-contact-provider
    public static void updateAvatars(final Context context, List<XWikiUserFull> userList) throws IOException {
        if (userList == null || userList.size() == 0) return;
        final ContentResolver resolver = context.getContentResolver();
        final Semaphore semaphore = new Semaphore(3);
        for (XWikiUserFull xwikiUser : userList) {
            final long rawContactId = lookupRawContact(resolver, xwikiUser.id);
            if (!TextUtils.isEmpty(xwikiUser.pageName) && !TextUtils.isEmpty(xwikiUser.getAvatar())) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AppContext.getApiManager().getXWikiPhotosManager().downloadAvatar(
                    xwikiUser.pageName, xwikiUser.getAvatar()
                ).subscribe(
                    new Action1<byte[]>() {
                        @Override
                        public void call(byte[] bytes) {
                            if (bytes != null) {
                                try {
                                    writeDisplayPhoto(context, rawContactId, bytes);
                                } catch (IOException e) {
                                    Log.e(
                                        TAG,
                                        "Can't update avatar of user",
                                        e
                                    );
                                }
                            }
                            semaphore.release();
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
                            semaphore.release();
                        }
                    }
                );
            }
        }
    }


    /**
     * Adds a single contact to the platform contacts provider.
     * This can be used to respond to a new contact found as part
     * of sync information returned from the server, or because a
     * user added a new contact.
     *  @param context        the Authenticator Activity context
     * @param accountName    the account the contact belongs to
     * @param user           the sample SyncAdapter User object
     * @param groupId        the id of the sample group
     * @param inSync         is the add part of a client-server sync?
     * @param batchOperation allow us to batch together multiple operations
     */
    public static void addContact(Context context, String accountName, XWikiUserFull user,
                                  long groupId, boolean inSync, BatchOperation batchOperation) {
        // Put the data in the contacts provider
        final ContactOperations contactOp = ContactOperations.createNewContact(
                context, user.id, accountName, inSync, batchOperation);

        //avoid that the contact information is empty in the phone's local address book.
//        if (TextUtils.isEmpty(user.getFirstName()) && TextUtils.isEmpty(user.getLastName())) {
//            user.get = user.pageName;
//        }

        contactOp.addName(
            user.getFullName(),
            user.getFirstName(),
            user.getLastName()
        )
            .addEmail(user.getEmail())
            .addPhone(user.getPhone(), Phone.TYPE_MOBILE);
        //.addPhone(user.getPhone(), Phone.TYPE_HOME)
        //.addPhone(user.getPhone(), Phone.TYPE_WORK)
        //.addGroupMembership(groupId)
        //.addAvatar(user.pageName, user.getAvatar());

        // If we have a serverId, then go ahead and create our status profile.
        // Otherwise skip it - and we'll create it after we sync-up to the
        // server later on.
        if (user.id != null) {
            contactOp.addProfileAction(user.id);
        }
    }


    /**
     * writeDisplayPhoto
     *
     * @param context
     * @param rawContactId the contact id
     * @param photo        the photo bytes
     *                     https://code.google.com/p/android/issues/detail?id=73499
     *                     https://forums.bitfire.at/topic/342/transactiontoolargeexception-when-syncing-contacts-with-high-res-images/5
     *                     http://developer.android.com/reference/android/provider/ContactsContract.RawContacts.DisplayPhoto.html
     */
    public static void writeDisplayPhoto(Context context, long rawContactId, byte[] photo) throws IOException {
        Uri rawContactPhotoUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
        AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(rawContactPhotoUri, "rw");
        OutputStream os = fd.createOutputStream();
        os.write(photo);
        os.close();
        fd.close();
    }

    /**
     * Updates a single contact to the platform contacts provider.
     * This method can be used to update a contact from a sync
     * operation or as a result of a user editing a contact
     * record.
     * <p>
     * This operation is actually relatively complex.  We query
     * the database to find all the rows of info that already
     * exist for this Contact. For rows that exist (and thus we're
     * modifying existing fields), we create an update operation
     * to change that field.  But for fields we're adding, we create
     * "add" operations to create new rows for those fields.
     *  @param context        the Authenticator Activity context
     * @param resolver       the ContentResolver to use
     * @param user           the sample SyncAdapter contact object
     * @param updateStatus   should we update this user's status
     * @param updateAvatar   should we update this user's avatar image
     * @param inSync         is the update part of a client-server sync?
     * @param rawContactId   the unique Id for this user in contacts
*                       provider
     * @param batchOperation allow us to batch together multiple operations
     */
    public static void updateContact(Context context, ContentResolver resolver,
                                     XWikiUserFull user, boolean updateServerId, boolean updateStatus, boolean updateAvatar,
                                     boolean inSync, long rawContactId, BatchOperation batchOperation) {

        boolean existingCellPhone = false;
        boolean existingHomePhone = false;
        boolean existingWorkPhone = false;
        boolean existingEmail = false;
        boolean existingAvatar = false;

        final Cursor c =
                resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                        new String[]{String.valueOf(rawContactId)}, null);
        final ContactOperations contactOp =
                ContactOperations.updateExistingContact(context, rawContactId,
                        inSync, batchOperation);

        //avoid that the contact information is empty in the phone's local address book.
//        if (TextUtils.isEmpty(user.firstName) && TextUtils.isEmpty(user.lastName)) {
//            user.firstName = user.pageName;
//        }

        try {
            // Iterate over the existing rows of data, and update each one
            // with the information we received from the server.
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                    contactOp.updateName(uri,
                            c.getString(DataQuery.COLUMN_GIVEN_NAME),
                            c.getString(DataQuery.COLUMN_FAMILY_NAME),
                            c.getString(DataQuery.COLUMN_FULL_NAME),
                            user.getFirstName(),
                            user.getLastName(),
                            null);
                } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == Phone.TYPE_MOBILE) {
                        existingCellPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                user.getPhone(), uri);
                    } else if (type == Phone.TYPE_HOME) {
                        existingHomePhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                user.getPhone(), uri);
                    } else if (type == Phone.TYPE_WORK) {
                        existingWorkPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                user.getPhone(), uri);
                    }
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                    existingEmail = true;
                    int type = c.getInt(DataQuery.COLUMN_EMAIL_TYPE);
                    if (type == ContactsContract.CommonDataKinds.Email.TYPE_WORK) {
                        contactOp.updateEmail(user.getEmail(),
                                c.getString(DataQuery.COLUMN_EMAIL_ADDRESS), uri);
                    }
                }
                //else if (mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                //existingAvatar = true;
                //contactOp.updateAvatar(user.pageName, user.getAvatar(), uri);
                //}
            } // while
        } finally {
            c.close();
        }

        // Add the cell phone, if present and not updated above
        if (!existingCellPhone) {
            contactOp.addPhone(user.getPhone(), Phone.TYPE_MOBILE);
        }
        // Add the home phone, if present and not updated above
        if (!existingHomePhone) {
            contactOp.addPhone(user.getPhone(), Phone.TYPE_HOME);
        }

        // Add the work phone, if present and not updated above
        if (!existingWorkPhone) {
            contactOp.addPhone(user.getPhone(), Phone.TYPE_WORK);
        }
        // Add the email address, if present and not updated above
        if (!existingEmail) {
            contactOp.addEmail(user.getEmail());
        }
        // Add the avatar if we didn't update the existing avatar
        //if (!existingAvatar) {
        //contactOp.addAvatar(user.pageName, user.getAvatar());
        //}

        // If we need to update the serverId of the contact record, take
        // care of that.  This will happen if the contact is created on the
        // client, and then synced to the server. When we get the updated
        // record back from the server, we can set the SOURCE_ID property
        // on the contact, so we can (in the future) lookup contacts by
        // the serverId.
        if (updateServerId) {
            Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
            contactOp.updateServerId(user.id, uri);
        }

        // If we don't have a status profile, then create one.  This could
        // happen for contacts that were created on the client - we don't
        // create the status profile until after the first sync...
        final String serverId = user.id;
        final long profileId = lookupProfile(resolver, serverId);
        if (profileId <= 0) {
            contactOp.addProfileAction(serverId);
        }
    }

    /**
     * get all contacts in hashMap<serverId, rawId>
     *
     * @param context
     * @param accountName
     * @return hashmap
     */
    public static HashMap<String, Long> getAllContactsIdMap(Context context, String accountName) {
        HashMap<String, Long> allMaps = new HashMap<>();
        final ContentResolver resolver = context.getContentResolver();
        final Cursor c = resolver.query(AllQuery.CONTENT_URI,
                AllQuery.PROJECTION,
                AllQuery.SELECTION,
                new String[]{accountName},
                null);
        try {
            while (c.moveToNext()) {
                final String serverId = c.getString(AllQuery.COLUMN_SERVER_ID);
                ;
                final long rawId = c.getLong(AllQuery.COLUMN_RAW_CONTACT_ID);
                ;
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
     * @param context      the Authenticator Activity context
     * @param rawContactId the unique ID for the local contact
     * @return a User object containing info on that contact
     */
    public static XWikiUser getXWikiUser(Context context, long rawContactId) {
        String firstName = null;
        String lastName = null;
        String fullName = null;
        String cellPhone = null;
        String homePhone = null;
        String workPhone = null;
        String email = null;
        String serverId = null;

        final ContentResolver resolver = context.getContentResolver();
        final Cursor c =
                resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                        new String[]{String.valueOf(rawContactId)}, null);
        try {
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final String tempServerId = c.getString(DataQuery.COLUMN_SERVER_ID);
                if (tempServerId != null) {
                    serverId = tempServerId;
                }
                final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                    lastName = c.getString(DataQuery.COLUMN_FAMILY_NAME);
                    firstName = c.getString(DataQuery.COLUMN_GIVEN_NAME);
                    //fullName = c.getString(DataQuery.COLUMN_FULL_NAME);
                } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == Phone.TYPE_MOBILE) {
                        cellPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                    } else if (type == Phone.TYPE_HOME) {
                        homePhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                    } else if (type == Phone.TYPE_WORK) {
                        workPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                    }
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                    email = c.getString(DataQuery.COLUMN_EMAIL_ADDRESS);
                }
            } // while
        } finally {
            c.close();
        }

        // Now that we've extracted all the information we care about,
        // create the actual User object.
        XWikiUser wikiUser = new XWikiUser(serverId, null, firstName, lastName, email, cellPhone, null,
                rawContactId, null, null, null, null, null, null);

        return wikiUser;
    }


    /**
     * When we first add a sync adapter to the system, the contacts from that
     * sync adapter will be hidden unless they're merged/grouped with an existing
     * contact.  But typically we want to actually show those contacts, so we
     * need to mess with the Settings table to get them to show up.
     *
     * @param context the Authenticator Activity context
     * @param account the Account who's visibility we're changing
     * @param visible true if we want the contacts visible, false for hidden
     */
    public static void setAccountContactsVisibility(Context context, Account account,
                                                    boolean visible) {
        ContentValues values = new ContentValues();
        values.put(RawContacts.ACCOUNT_NAME, account.name);
        values.put(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        values.put(Settings.UNGROUPED_VISIBLE, visible ? 1 : 0);

        context.getContentResolver().insert(Settings.CONTENT_URI, values);
    }

    /**
     * Deletes a contact from the platform contacts provider. This method is used
     * both for contacts that were deleted locally and then that deletion was synced
     * to the server, and for contacts that were deleted on the server and the
     * deletion was synced to the client.
     *
     * @param context      the Authenticator Activity context
     * @param rawContactId the unique Id for this rawContact in contacts
     *                     provider
     */
    private static void deleteContact(Context context, long rawContactId,
                                      BatchOperation batchOperation) {
        batchOperation.add(ContactOperations.newDeleteCpo(
                ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                true, true).build());
    }

    /**
     * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
     * sample SyncAdapter user isn't found.
     *
     * @param resolver        the content resolver to use
     * @param serverContactId the sample SyncAdapter user ID to lookup
     * @return the RawContact id, or 0 if not found
     */
    private static long lookupRawContact(ContentResolver resolver, String serverContactId) {

        long rawContactId = 0;
        final Cursor c = resolver.query(
                UserIdQuery.CONTENT_URI,
                UserIdQuery.PROJECTION,
                UserIdQuery.SELECTION,
                new String[]{String.valueOf(serverContactId)},
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
                resolver.query(Data.CONTENT_URI, ProfileQuery.PROJECTION, ProfileQuery.SELECTION,
                        new String[]{String.valueOf(userId)}, null);
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

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    final private static class ProfileQuery {

        private ProfileQuery() {
        }

        public final static String[] PROJECTION = new String[]{Data._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
                Data.MIMETYPE + "='" + ContactColumns.MIME_PROFILE + "' AND "
                        + ContactColumns.DATA_PID + "=?";
    }

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    final private static class UserIdQuery {

        private UserIdQuery() {
        }

        public final static String[] PROJECTION = new String[]{
                RawContacts._ID,
                RawContacts.CONTACT_ID
        };

        public final static int COLUMN_RAW_CONTACT_ID = 0;
        public final static int COLUMN_LINKED_CONTACT_ID = 1;

        public final static Uri CONTENT_URI = RawContacts.CONTENT_URI;

        public static final String SELECTION =
                RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND "
                        + RawContacts.SOURCE_ID + "=?";
    }

    /**
     * Constants for a query to get contact data for a given rawContactId
     */
    final private static class DataQuery {

        private DataQuery() {
        }

        public static final String[] PROJECTION =
                new String[]{Data._ID, RawContacts.SOURCE_ID, Data.MIMETYPE, Data.DATA1,
                        Data.DATA2, Data.DATA3, Data.DATA15, Data.SYNC1};

        public static final int COLUMN_ID = 0;
        public static final int COLUMN_SERVER_ID = 1;
        public static final int COLUMN_MIMETYPE = 2;
        public static final int COLUMN_DATA1 = 3;
        public static final int COLUMN_DATA2 = 4;
        public static final int COLUMN_DATA3 = 5;
        public static final int COLUMN_SYNC1 = 7;

        public static final Uri CONTENT_URI = Data.CONTENT_URI;

        public static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
        public static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;
        public static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
        public static final int COLUMN_EMAIL_TYPE = COLUMN_DATA2;
        public static final int COLUMN_FULL_NAME = COLUMN_DATA1;
        public static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
        public static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;
        public static final int COLUMN_SYNC_DIRTY = COLUMN_SYNC1;

        public static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
    }

    final private static class AllQuery {

        private AllQuery() {
        }

        public final static String[] PROJECTION = new String[]{
                RawContacts._ID,
                RawContacts.SOURCE_ID,
        };

        public final static int COLUMN_RAW_CONTACT_ID = 0;
        public final static int COLUMN_SERVER_ID = 1;

        public static final Uri CONTENT_URI = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();

        public static final String SELECTION =
                RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND "
                        + RawContacts.ACCOUNT_NAME + "=?";
    }

    /**
     * Constants for a query to read basic contact columns
     */
    final public static class ContactQuery {
        private ContactQuery() {
        }

        public static final String[] PROJECTION =
                new String[]{Contacts._ID, Contacts.DISPLAY_NAME};

        public static final int COLUMN_ID = 0;
        public static final int COLUMN_DISPLAY_NAME = 1;
    }
}
