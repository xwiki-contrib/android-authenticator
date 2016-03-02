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

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.util.Log;

import org.xwiki.android.authenticator.rest.XWikiUser;

import java.util.List;

/**
 * Class for managing contacts sync related mOperations
 */
public class ContactManager {
    private static final String TAG = "ContactManager";

    public static final String SAMPLE_GROUP_NAME = "XWiki Group";

    public static long ensureXWikiGroupExists(Context context, Account account) {
        final ContentResolver resolver = context.getContentResolver();

        // Lookup the sample group
        long groupId = 0;
        final Cursor cursor = resolver.query(Groups.CONTENT_URI, new String[] { Groups._ID },
                Groups.ACCOUNT_NAME + "=? AND " + Groups.ACCOUNT_TYPE + "=? AND " +
                Groups.TITLE + "=?",
                new String[] { account.name, account.type, SAMPLE_GROUP_NAME }, null);
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
            // Sample group doesn't exist yet, so create it
            final ContentValues contentValues = new ContentValues();
            contentValues.put(Groups.ACCOUNT_NAME, account.name);
            contentValues.put(Groups.ACCOUNT_TYPE, account.type);
            contentValues.put(Groups.TITLE, SAMPLE_GROUP_NAME);
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
     * @param context The context of Authenticator Activity
     * @param account The username for the account
     * @param rawContacts The list of contacts to update
     * @param groupId the id of the sample group
     * @param lastSyncDate The previous server sync-state
     * @return the server syncState that should be used in our next
     * sync request.
     */
    public static synchronized long updateContacts(Context context, String account,
            List<XWikiUser> rawContacts, long groupId, long lastSyncDate) {

        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(context, resolver);

        // Add new contact and update changed ones

        long currentSyncDate = lastSyncDate;

        Log.d(TAG, "Synchronizing XWiki contacts");
        for (final XWikiUser xwikiUser : rawContacts) {
            if (xwikiUser.getDate() > lastSyncDate) {
                if (xwikiUser.getDate() > currentSyncDate) {
                    currentSyncDate = xwikiUser.getDate();
                }

                /*if (exist(xwikiUser.getId())) {
                    Log.d(TAG, "Update contact");
                    updateContact(context, resolver, xwikiUser, updateServerId, true, true, true, rawContactId, batchOperation);
                } else {
                    Log.d(TAG, "Add contact");
                    addContact(context, account, xwikiUser, groupId, true, batchOperation);
                }*/
                // A sync adapter should batch operations on multiple contacts,
                // because it will make a dramatic performance difference.
                // (UI updates, etc)
                if (batchOperation.size() >= 50) {
                    batchOperation.execute();
                }
            }
        }
        batchOperation.execute();

        // TODO: Remove contacts that don't exist anymore

        return currentSyncDate;
    }

    /**
     * Adds a single contact to the platform contacts provider.
     * This can be used to respond to a new contact found as part
     * of sync information returned from the server, or because a
     * user added a new contact.
     *
     * @param context the Authenticator Activity context
     * @param accountName the account the contact belongs to
     * @param rawContact the sample SyncAdapter User object
     * @param groupId the id of the sample group
     * @param inSync is the add part of a client-server sync?
     * @param batchOperation allow us to batch together multiple operations
     *        into a single provider call
     */
    public static void addContact(Context context, String accountName, RawContact rawContact,
            long groupId, boolean inSync, BatchOperation batchOperation) {

        // Put the data in the contacts provider
        final ContactOperations contactOp = ContactOperations.createNewContact(
                context, rawContact.getServerContactId(), accountName, inSync, batchOperation);

        contactOp.addName(rawContact.getFullName(), rawContact.getFirstName(),
                rawContact.getLastName())
                .addEmail(rawContact.getEmail())
                .addPhone(rawContact.getCellPhone(), Phone.TYPE_MOBILE)
                .addPhone(rawContact.getHomePhone(), Phone.TYPE_HOME)
                .addPhone(rawContact.getOfficePhone(), Phone.TYPE_WORK)
                //.addGroupMembership(groupId)
                .addAvatar(rawContact.getAvatarUrl());

        // If we have a serverId, then go ahead and create our status profile.
        // Otherwise skip it - and we'll create it after we sync-up to the
        // server later on.
        if (rawContact.getServerContactId() > 0) {
            //contactOp.addProfileAction(rawContact.getServerContactId());
        }
    }

    /**
     * Updates a single contact to the platform contacts provider.
     * This method can be used to update a contact from a sync
     * operation or as a result of a user editing a contact
     * record.
     *
     * This operation is actually relatively complex.  We query
     * the database to find all the rows of info that already
     * exist for this Contact. For rows that exist (and thus we're
     * modifying existing fields), we create an update operation
     * to change that field.  But for fields we're adding, we create
     * "add" operations to create new rows for those fields.
     *
     * @param context the Authenticator Activity context
     * @param resolver the ContentResolver to use
     * @param rawContact the sample SyncAdapter contact object
     * @param updateStatus should we update this user's status
     * @param updateAvatar should we update this user's avatar image
     * @param inSync is the update part of a client-server sync?
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     * @param batchOperation allow us to batch together multiple operations
     *        into a single provider call
     */
    public static void updateContact(Context context, ContentResolver resolver,
        RawContact rawContact, boolean updateServerId, boolean updateStatus, boolean updateAvatar,
        boolean inSync, long rawContactId, BatchOperation batchOperation) {
/*
        boolean existingCellPhone = false;
        boolean existingHomePhone = false;
        boolean existingWorkPhone = false;
        boolean existingEmail = false;

        final Cursor c =
                resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                new String[] {String.valueOf(rawContactId)}, null);
        final ContactOperations contactOp =
                ContactOperations.updateExistingContact(context, rawContactId,
                inSync, batchOperation);
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
                            rawContact.getFirstName(),
                            rawContact.getLastName(),
                            rawContact.getFullName());
                } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == Phone.TYPE_MOBILE) {
                        existingCellPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                rawContact.getCellPhone(), uri);
                    } else if (type == Phone.TYPE_HOME) {
                        existingHomePhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                rawContact.getHomePhone(), uri);
                    } else if (type == Phone.TYPE_WORK) {
                        existingWorkPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                rawContact.getOfficePhone(), uri);
                    }
                } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                    existingEmail = true;
                    contactOp.updateEmail(rawContact.getEmail(),
                            c.getString(DataQuery.COLUMN_EMAIL_ADDRESS), uri);
                } else if (mimeType.equals(Photo.CONTENT_ITEM_TYPE)) {
                    existingAvatar = true;
                    contactOp.updateAvatar(rawContact.getAvatarUrl(), uri);
                }
            } // while
        } finally {
            c.close();
        }

        // Add the cell phone, if present and not updated above
        if (!existingCellPhone) {
            contactOp.addPhone(rawContact.getCellPhone(), Phone.TYPE_MOBILE);
        }
        // Add the home phone, if present and not updated above
        if (!existingHomePhone) {
            contactOp.addPhone(rawContact.getHomePhone(), Phone.TYPE_HOME);
        }

        // Add the work phone, if present and not updated above
        if (!existingWorkPhone) {
            contactOp.addPhone(rawContact.getOfficePhone(), Phone.TYPE_WORK);
        }
        // Add the email address, if present and not updated above
        if (!existingEmail) {
            contactOp.addEmail(rawContact.getEmail());
        }
        // Add the avatar if we didn't update the existing avatar
        if (!existingAvatar) {
            contactOp.addAvatar(rawContact.getAvatarUrl());
        }

        // If we need to update the serverId of the contact record, take
        // care of that.  This will happen if the contact is created on the
        // client, and then synced to the server. When we get the updated
        // record back from the server, we can set the SOURCE_ID property
        // on the contact, so we can (in the future) lookup contacts by
        // the serverId.
        if (updateServerId) {
            Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
            contactOp.updateServerId(rawContact.getServerContactId(), uri);
        }

        // If we don't have a status profile, then create one.  This could
        // happen for contacts that were created on the client - we don't
        // create the status profile until after the first sync...
        final long serverId = rawContact.getServerContactId();
        final long profileId = lookupProfile(resolver, serverId);
        if (profileId <= 0) {
            contactOp.addProfileAction(serverId);
        }
        */
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
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
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
     * @param resolver the content resolver to use
     * @param serverContactId the sample SyncAdapter user ID to lookup
     * @return the RawContact id, or 0 if not found
     */
    private static long lookupRawContact(ContentResolver resolver, long serverContactId) {

        long rawContactId = 0;
        final Cursor c = resolver.query(
                UserIdQuery.CONTENT_URI,
                UserIdQuery.PROJECTION,
                UserIdQuery.SELECTION,
                new String[] {String.valueOf(serverContactId)},
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
     * @param userId the sample SyncAdapter user ID to lookup
     * @return the profile Data row id, or 0 if not found
     */
    private static long lookupProfile(ContentResolver resolver, long userId) {

        long profileId = 0;
        final Cursor c =
            resolver.query(Data.CONTENT_URI, ProfileQuery.PROJECTION, ProfileQuery.SELECTION,
                new String[] {String.valueOf(userId)}, null);
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

        public final static String[] PROJECTION = new String[] {Data._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
            Data.MIMETYPE + "='" + SampleSyncAdapterColumns.MIME_PROFILE + "' AND "
                + SampleSyncAdapterColumns.DATA_PID + "=?";
    }

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    final private static class UserIdQuery {

        private UserIdQuery() {
        }

        public final static String[] PROJECTION = new String[] {
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
            new String[] {Data._ID, RawContacts.SOURCE_ID, Data.MIMETYPE, Data.DATA1,
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

    /**
     * Constants for a query to read basic contact columns
     */
    final public static class ContactQuery {
        private ContactQuery() {
        }

        public static final String[] PROJECTION =
            new String[] {Contacts._ID, Contacts.DISPLAY_NAME};

        public static final int COLUMN_ID = 0;
        public static final int COLUMN_DISPLAY_NAME = 1;
    }
}
