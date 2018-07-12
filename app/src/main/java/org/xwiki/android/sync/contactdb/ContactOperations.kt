package org.xwiki.android.sync.contactdb

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.provider.ContactsContract
import org.xwiki.android.sync.Constants
import org.xwiki.android.sync.bean.XWikiUserFull
import android.content.ContentValues
import android.net.Uri
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.R

/**
 * Mime type for insert in database to let android OS know which filter must be activated
 * for editing contact of XWiki account
 *
 * @since 0.5
 */
private const val EDIT_CONTACT_MIME_TYPE = "vnd.android.cursor.item/vnd.xwikiedit.profile"

/**
 * Field which will contains user id
 */
private const val EDIT_CONTACT_USER_ID_FIELD = ContactsContract.Data.DATA1

/**
 * Field which will contains text for edit contact button
 */
private const val EDIT_CONTACT_TEXT_FIELD = ContactsContract.Data.DATA3

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
 * @since 0.5
 */
fun setAccountContactsVisibility(
    resolver: ContentResolver,
    account: Account,
    visible: Boolean
) {
    val values = ContentValues()
    values.put(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
    values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
    values.put(ContactsContract.Settings.UNGROUPED_VISIBLE, if (visible) 1 else 0)

    resolver.insert(ContactsContract.Settings.CONTENT_URI, values)
}

fun clearOldAccountContacts(
    resolver: ContentResolver,
    account: Account
) {
    resolver.applyBatch(
        ContactsContract.AUTHORITY,
        ArrayList<ContentProviderOperation>().also {
            it.add(
                ContentProviderOperation.newDelete(
                    ContactsContract.RawContacts.CONTENT_URI
                ).run {
                    withSelection("${ContactsContract.RawContacts.ACCOUNT_NAME}=?", arrayOf(account.name))
                    build()
                }
            )
        }
    )
}

/**
 * Search user raw in android contacts. If not exists - will create new one and return this rowId
 *
 * @param resolver Will be used to search exist user or create new one
 * @param accountName Account name for search
 *
 * @return Row id of user
 *
 * @since 0.5
 */
fun XWikiUserFull.rowId(
    resolver: ContentResolver,
    accountName: String
): Long {
    resolver.query(
        ContactsContract.RawContacts.CONTENT_URI,
        arrayOf(ContactsContract.Data._ID),
        "${ContactsContract.RawContacts.ACCOUNT_TYPE}=\"${Constants.ACCOUNT_TYPE}\" AND " +
            "${ContactsContract.RawContacts.SOURCE_ID}=\"$id\"",
        null,
        null
    ) ?.use {
        return if (it.moveToFirst()) {
            it.getLong(
                it.getColumnIndex(ContactsContract.Data._ID)
            )
        } else {
            val rawContactUri = resolver.insert(
                ContactsContract.RawContacts.CONTENT_URI,
                ContentValues().apply {
                    put(ContactsContract.RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
                    put(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                    put(ContactsContract.RawContacts.SOURCE_ID, id)
                }
            )
            ContentUris.parseId(
                rawContactUri
            )
        }
    } ?: throw IllegalStateException("Can't get or create row id for user")
}

/**
 * Create new delete operation for contact
 *
 * @param rowId Contact row id
 *
 * @see {@link #rowId(ContentResolver, String)}
 *
 * @since 0.5
 */
private fun clearOldUserData(
    rowId: Long
): ContentProviderOperation {
    return ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI).run {
        withSelection("${ContactsContract.Data.RAW_CONTACT_ID}=?", arrayOf(rowId.toString()))
        build()
    }
}

/**
 * Create new insert operation with pairs
 *
 * @param rowId Contact row id
 * @param mimeType Type mime such as {@link ContactsContract.CommonDataKinds.StructuredName#CONTENT_ITEM_TYPE}
 * @param dataPairs Pairs of data to insert
 *
 * @see {@link #rowId(ContentResolver, String)}
 *
 * @since 0.5
 */
private fun createContentProviderOperation(
    rowId: Long,
    mimeType: String,
    dataPairs: Map<String, Any>
): ContentProviderOperation {
    return ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).run {
        withValue(ContactsContract.Data.RAW_CONTACT_ID, rowId)
        withValue(
            ContactsContract.Data.MIMETYPE,
            mimeType
        )
        dataPairs.forEach { (k, v) -> withValue(k, v) }
        build()
    }
}

/**
 * Contains equivalent operations which creates {@link ContentProviderOperation} objects
 * for each different type of user info using as base some {@link XWikiUserFull} context
 */
private val propertiesToContentProvider = listOf<XWikiUserFull.(Long) -> ContentProviderOperation>(
    {// user name filling
        rowId ->
        createContentProviderOperation(
            rowId,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            mapOf(
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME to firstName,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME to lastName
            )
        )
    },
    {// user address filling
        rowId ->
        createContentProviderOperation(
            rowId,
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
            mapOf(
                ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY to country,
                ContactsContract.CommonDataKinds.StructuredPostal.CITY to city,
                ContactsContract.CommonDataKinds.StructuredPostal.STREET to address
            )
        )
    },
    {// user phone filling
        rowId ->
        createContentProviderOperation(
            rowId,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            mapOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER to phone,
                ContactsContract.CommonDataKinds.Phone.TYPE to ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            )
        )
    },
    {// user company filling
        rowId ->
        createContentProviderOperation(
            rowId,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
            mapOf(
                ContactsContract.CommonDataKinds.Organization.COMPANY to company
            )
        )
    },
    {// user email filling
        rowId ->
        createContentProviderOperation(
            rowId,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            mapOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS to email
            )
        )
    },
    {// user comment filling
        rowId ->
        createContentProviderOperation(
            rowId,
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
            mapOf(
                ContactsContract.CommonDataKinds.Note.NOTE to comment
            )
        )
    },
    {
        rowId ->
        createContentProviderOperation(
            rowId,
            EDIT_CONTACT_MIME_TYPE,
            mapOf(
                EDIT_CONTACT_USER_ID_FIELD to convertId(),
                EDIT_CONTACT_TEXT_FIELD to AppContext.getInstance().getString(R.string.edit)
            )
        )
    }
)

fun getContactRowId(
    resolver: ContentResolver,
    from: Uri
): Long? {
    return resolver.query(
        from,
        arrayOf(ContactsContract.Contacts.Data.RAW_CONTACT_ID),
        null,
        null,
        null
    ).use {
        if (it.moveToFirst()) {
            it.getLong(
                it.getColumnIndex(
                    ContactsContract.Contacts.Data.RAW_CONTACT_ID
                )
            )
        } else {
            null
        }
    }
}

fun getContactUserId(
    resolver: ContentResolver,
    rowId: Long
): String? {
    return resolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(EDIT_CONTACT_USER_ID_FIELD),
        "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
        arrayOf(
            rowId.toString(),
            EDIT_CONTACT_MIME_TYPE
        ),
        null
    ).use {
        if (it.moveToFirst()) {
            it.getString(
                it.getColumnIndex(
                    EDIT_CONTACT_USER_ID_FIELD
                )
            )
        } else {
            null
        }
    }
}

/**
 * Will create contact or update existing using context user
 *
 * @since 0.5
 */
fun XWikiUserFull.toContentProviderOperations(
    resolver: ContentResolver,
    accountName: String
): List<ContentProviderOperation> {
    val rowId: Long = rowId(resolver, accountName)
    
    return listOf(
        clearOldUserData(rowId),
        *propertiesToContentProvider.map {
            it(rowId)
        }.toTypedArray()
    )
}
