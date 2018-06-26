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
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;

/**
 * Helper class for storing data in the platform content providers.
 *
 * @version $Id$
 */
public class ContactOperations {

    /**
     * Contains all data of contact
     */
    private final ContentValues mValues;

    /**
     * Contains batch operation which will be used to register actions
     */
    private final BatchOperation mBatchOperation;

    /**
     * Context of operation
     */
    private final Context mContext;

    /**
     * Flag about the state of operation
     */
    private boolean mIsSyncOperation;

    /**
     * Raw id of contact
     */
    private long mRawContactId;

    /**
     * Back reference of contact
     */
    private int mBackReference;

    /**
     * Will be used to detect insert (if new) / update (if old) operation
     */
    private boolean mIsNewContact;

    /**
     * Since we're sending a lot of contact provider operations in a single
     * batched operation, we want to make sure that we "yield" periodically
     * so that the Contact Provider can write changes to the DB, and can
     * open a new transaction.  This prevents ANR (application not responding)
     * errors.  The recommended time to specify that a yield is permitted is
     * with the first operation on a particular contact.  So if we're updating
     * multiple fields for a single contact, we make sure that we call
     * withYieldAllowed(true) on the first field that we update. We use
     * mIsYieldAllowed to keep track of what value we should pass to
     * withYieldAllowed().
     */
    private boolean mIsYieldAllowed;

    /**
     * Returns an instance of ContactOperations instance for adding new contact
     * to the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param userId the userId of the sample SyncAdapter user object
     * @param accountName the username for the SyncAdapter account
     * @param isSyncOperation are we executing this as part of a sync operation
     * @param batchOperation BatchOperation for task context
     * @return instance of ContactOperations
     */
    public static ContactOperations createNewContact(
        Context context,
        String userId,
        String accountName,
        boolean isSyncOperation,
        BatchOperation batchOperation
    ) {
        return new ContactOperations(context, userId, accountName, isSyncOperation, batchOperation);
    }

    /**
     * Returns an instance of ContactOperations for updating existing contact in
     * the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id of the existing rawContact
     * @param isSyncOperation are we executing this as part of a sync operation
     * @param batchOperation BatchOperation for task context
     * @return instance of ContactOperations
     */
    public static ContactOperations updateExistingContact(
        Context context,
        long rawContactId,
        boolean isSyncOperation,
        BatchOperation batchOperation
    ) {
        return new ContactOperations(context, rawContactId, isSyncOperation, batchOperation);
    }

    /**
     * Returns an instance of ContactOperations for updating existing contact in
     * the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param isSyncOperation are we executing this as part of a sync operation
     * @param batchOperation BatchOperation for task context
     */
    public ContactOperations(
        Context context,
        boolean isSyncOperation,
        BatchOperation batchOperation
    ) {
        mValues = new ContentValues();
        mIsYieldAllowed = true;
        mIsSyncOperation = isSyncOperation;
        mContext = context;
        mBatchOperation = batchOperation;
    }

    /**
     * Returns an instance of ContactOperations for updating existing contact in
     * the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param userId Contact user id
     * @param accountName Contact username
     * @param isSyncOperation are we executing this as part of a sync operation
     * @param batchOperation BatchOperation for task context
     */
    public ContactOperations(
        Context context,
        String userId,
        String accountName,
        boolean isSyncOperation,
        BatchOperation batchOperation
    ) {
        this(context, isSyncOperation, batchOperation);
        mBackReference = mBatchOperation.size();
        mIsNewContact = true;
        mValues.put(RawContacts.SOURCE_ID, userId);
        mValues.put(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        mValues.put(RawContacts.ACCOUNT_NAME, accountName);
        ContentProviderOperation.Builder builder = newInsertCpo(
            RawContacts.CONTENT_URI,
            mIsSyncOperation,
            true
        ).withValues(
            mValues
        );
        mBatchOperation.add(builder.build());
    }

    /**
     * Returns an instance of ContactOperations for updating existing contact in
     * the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id of the existing rawContact
     * @param isSyncOperation are we executing this as part of a sync operation
     * @param batchOperation BatchOperation for task context
     */
    public ContactOperations(
        Context context,
        long rawContactId,
        boolean isSyncOperation,
        BatchOperation batchOperation
    ) {
        this(context, isSyncOperation, batchOperation);
        mIsNewContact = false;
        mRawContactId = rawContactId;
    }

    /**
     * Adds a contact name. We can take either a full name ("Bob Smith") or separated
     * first-name and last-name ("Bob" and "Smith").
     *
     * @param fullName  The full name of the contact - typically from an edit form
     *                  Can be null if firstName/lastName are specified.
     * @param firstName The first name of the contact - can be null if fullName
     *                  is specified.
     * @param lastName  The last name of the contact - can be null if fullName
     *                  is specified.
     * @return instance of ContactOperations
     */
    public ContactOperations addName(String fullName, String firstName, String lastName) {
        mValues.clear();

        if (!TextUtils.isEmpty(fullName)) {
            mValues.put(StructuredName.DISPLAY_NAME, fullName);
            mValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        } else {
            if (!TextUtils.isEmpty(firstName)) {
                mValues.put(StructuredName.GIVEN_NAME, firstName);
                mValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            }
            if (!TextUtils.isEmpty(lastName)) {
                mValues.put(StructuredName.FAMILY_NAME, lastName);
                mValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            }
        }
        if (mValues.size() > 0) {
            addInsertOp();
        }
        return this;
    }

    /**
     * Adds an email.
     *
     * @param email address we're adding
     * @return instance of ContactOperations
     */
    public ContactOperations addEmail(String email) {
        mValues.clear();
        if (!TextUtils.isEmpty(email)) {
            mValues.put(Email.DATA, email);
            mValues.put(Email.TYPE, Email.TYPE_WORK);
            mValues.put(Email.MIMETYPE, Email.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    /**
     * Adds a phone number.
     *
     * @param phone     new phone number for the contact
     * @param phoneType the type: cell, home, etc.
     * @return instance of ContactOperations
     */
    public ContactOperations addPhone(String phone, int phoneType) {
        mValues.clear();
        if (!TextUtils.isEmpty(phone)) {
            mValues.put(Phone.NUMBER, phone);
            mValues.put(Phone.TYPE, phoneType);
            mValues.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    /**
     * Adds a profile action.
     *
     * @param userId the userId of the sample SyncAdapter user object
     * @return instance of ContactOperations
     */
    public ContactOperations addProfileAction(String userId) {
        mValues.clear();
        if (userId != null) {
            mValues.put(ContactColumns.DATA_PID, userId);
            mValues.put(ContactColumns.DATA_SUMMARY, mContext
                    .getString(R.string.profile_action));
            mValues.put(ContactColumns.DATA_DETAIL, mContext
                    .getString(R.string.view_profile));
            mValues.put(Data.MIMETYPE, ContactColumns.MIME_PROFILE);
            addInsertOp();
        }
        return this;
    }

    /**
     * Updates contact's email.
     *
     * @param email email id of the sample SyncAdapter user
     * @param uri   Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updateEmail(String email, String existingEmail, Uri uri) {
        if (!TextUtils.equals(existingEmail, email)) {
            mValues.clear();
            mValues.put(Email.DATA, email);
            addUpdateOp(uri);
        }
        return this;
    }

    /**
     * Updates contact's name. The caller can either provide first-name
     * and last-name fields or a full-name field.
     *
     * @param uri Uri for the existing raw contact to be updated
     * @param existingFirstName the first name stored in provider
     * @param existingLastName the last name stored in provider
     * @param existingFullName the full name stored in provider
     * @param firstName the new first name to store
     * @param lastName the new last name to store
     * @param fullName the new full name to store
     * @return instance of ContactOperations
     */
    public ContactOperations updateName(
        Uri uri,
        String existingFirstName,
        String existingLastName,
        String existingFullName,
        String firstName,
        String lastName,
        String fullName
    ) {

        mValues.clear();
        if (TextUtils.isEmpty(fullName)) {
            if (!TextUtils.equals(existingFirstName, firstName)) {
                mValues.put(StructuredName.GIVEN_NAME, firstName);
            }
            if (!TextUtils.equals(existingLastName, lastName)) {
                mValues.put(StructuredName.FAMILY_NAME, lastName);
            }
        } else {
            if (!TextUtils.equals(existingFullName, fullName)) {
                mValues.put(StructuredName.DISPLAY_NAME, fullName);
            }
        }
        if (mValues.size() > 0) {
            addUpdateOp(uri);
        }
        return this;
    }

    /**
     * Updates contact's phone.
     *
     * @param existingNumber phone number stored in contacts provider
     * @param phone new phone number for the contact
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updatePhone(String existingNumber, String phone, Uri uri) {
        if (!TextUtils.equals(phone, existingNumber)) {
            mValues.clear();
            mValues.put(Phone.NUMBER, phone);
            addUpdateOp(uri);
        }
        return this;
    }

    /**
     * Adds an insert operation into the batch.
     */
    private void addInsertOp() {

        if (!mIsNewContact) {
            mValues.put(Phone.RAW_CONTACT_ID, mRawContactId);
        }
        ContentProviderOperation.Builder builder = newInsertCpo(
            Data.CONTENT_URI,
            mIsSyncOperation,
            mIsYieldAllowed);
        builder.withValues(mValues);
        if (mIsNewContact) {
            builder.withValueBackReference(Data.RAW_CONTACT_ID, mBackReference);
        }
        mIsYieldAllowed = false;
        mBatchOperation.add(builder.build());
    }

    /**
     * Adds an update operation into the batch.
     *
     * @param uri Uri to add operation
     * @see #newUpdateCpo(Uri, boolean, boolean)
     */
    private void addUpdateOp(Uri uri) {
        ContentProviderOperation.Builder builder = newUpdateCpo(
            uri,
            mIsSyncOperation,
            mIsYieldAllowed
        ).withValues(
            mValues
        );
        mIsYieldAllowed = false;
        mBatchOperation.add(builder.build());
    }

    /**
     * Prepare builder to insert operation
     *
     * @param uri Uri to add an operation
     * @param isSyncOperation in-sync flag
     * @param isYieldAllowed Yield allowed flag, more:
     * {@link ContentProviderOperation.Builder#withYieldAllowed(boolean)}
     * @return Result builder which already prepared to build insert operation
     */
    private static ContentProviderOperation.Builder newInsertCpo(
        Uri uri,
        boolean isSyncOperation,
        boolean isYieldAllowed
    ) {
        return ContentProviderOperation.newInsert(
            addCallerIsSyncAdapterParameter(
                uri,
                isSyncOperation
            )
        ).withYieldAllowed(
            isYieldAllowed
        );
    }

    /**
     * Prepare builder to update operation
     *
     * @param uri Uri to add an operation
     * @param isSyncOperation in-sync flag
     * @param isYieldAllowed Yield allowed flag, more:
     * {@link ContentProviderOperation.Builder#withYieldAllowed(boolean)}
     * @return Result builder which already prepared to build update operation
     */
    private static ContentProviderOperation.Builder newUpdateCpo(
        Uri uri,
        boolean isSyncOperation,
        boolean isYieldAllowed
    ) {
        return ContentProviderOperation.newUpdate(
            addCallerIsSyncAdapterParameter(
                uri,
                isSyncOperation
            )
        ).withYieldAllowed(
            isYieldAllowed
        );
    }

    /**
     * Prepare builder to delete operation
     *
     * @param uri Uri to add an operation
     * @param isSyncOperation in-sync flag
     * @param isYieldAllowed Yield allowed flag, more:
     * {@link ContentProviderOperation.Builder#withYieldAllowed(boolean)}
     * @return Result builder which already prepared to build delete operation
     */
    public static ContentProviderOperation.Builder newDeleteCpo(
        Uri uri,
        boolean isSyncOperation,
        boolean isYieldAllowed
    ) {
        return ContentProviderOperation.newDelete(
            addCallerIsSyncAdapterParameter(
                uri,
                isSyncOperation
            )
        ).withYieldAllowed(
            isYieldAllowed
        );
    }

    /**
     * If we're in the middle of a real sync-adapter (isSyncOperation == true) operation,
     * then go ahead and tell the Contacts provider that we're the sync adapter. That gives us
     * some special permissions - like the ability to really delete a contact, and the ability to
     * clear the dirty flag.
     *
     * <p>
     * If we're not in the middle of a sync operation (isSyncOperation == false) (for example, we
     * just locally created/edited a new contact), then we don't want to use the special
     * permissions, and the system will automatically mark the contact as 'dirty' for us!
     *
     * @param uri Base uri
     * @param isSyncOperation sync operation flag
     * @return uri or adapted uri if isSyncOperation == true
     */
    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER,
                "true"
            ).build();
        }
        return uri;
    }
}
