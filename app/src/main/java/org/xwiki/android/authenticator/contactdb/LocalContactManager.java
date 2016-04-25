package org.xwiki.android.authenticator.contactdb;

import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by fitz on 2016/4/20.
 */
public class LocalContactManager {

    public static void localCreateContact(Context context){
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        Log.i("Line38", "Here");
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, AccountManager.KEY_ACCOUNT_TYPE)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, AccountManager.KEY_ACCOUNT_NAME)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "u232786seee")
                .withValue(ContactsContract.CommonDataKinds.StructuredName.IN_VISIBLE_GROUP, true)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,"23232343434")
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "4343")
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, "")
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, "")
                .build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static String fetchContacts(Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        //ContactsContract.Contacts.CONTENT_URI
        Cursor people = cr.query(uri, null, null, null, null);
        Log.i("cursor", people.toString());
        String allContacts = "";
        if (people.getCount() > 0) {
            while (people.moveToNext()) {
                String id = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));
                String rawContactId = people.getString(people.getColumnIndex(ContactsContract.Contacts.Data.RAW_CONTACT_ID));
                int nameFieldColumnIndex = people.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String contact = people.getString(nameFieldColumnIndex);
                int numberFieldColumnIndex = people.getColumnIndex(ContactsContract.PhoneLookup.NUMBER);
                //String number = people.getString(numberFieldColumnIndex);
                String number = "";
                if (Integer.parseInt(people.getString(
                        people.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String email = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                        number += email + "\n";
                    }
                    allContacts += contact + ": " + number+",id="+rawContactId+".";
                    pCur.close();
                }
            }
        }
        people.close();
        return allContacts;
    }

    public static void updateContact(){
//        ContentValues values = new ContentValues();
//        values.put(Phone.NUMBER, "13800138000");
//        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, Phone.TYPE_MOBILE);
//        String where = ContactsContract.Data.RAW_CONTACT_ID + "=? AND "
//                + ContactsContract.Data.MIMETYPE + "=?";
//        String[] selectionArgs = new String[] { String.valueOf(rawContactId),
//                Phone.CONTENT_ITEM_TYPE };
//        getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
//                where, selectionArgs);
    }

}
