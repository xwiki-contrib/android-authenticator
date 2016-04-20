package org.xwiki.android.authenticator.activities;

import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.syncadapter.BatchOperation;
import org.xwiki.android.authenticator.syncadapter.ContactManager;
import org.xwiki.android.authenticator.syncadapter.RawContact;

import java.util.ArrayList;


/**
 * A login screen that offers login via email/password.
 */
public class EditContactActivity extends AppCompatActivity {

    // UI references.
    private EditText mFullNameView;
    private EditText mEmailView;
    private EditText mCellPhoneView;
    private EditText mWorkPhoneView;

    private TextView mContactInfoTextView;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_edit_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        StatusBarCompat.compat(this, Color.parseColor("#0077D9"));

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mFullNameView = (EditText) findViewById(R.id.fullname);
        mCellPhoneView = (EditText) findViewById(R.id.cellphone);
        mWorkPhoneView = (EditText) findViewById(R.id.workphone);

        mUri = getIntent().getData();
        mContactInfoTextView = (TextView) findViewById(R.id.contactinfo);

//        fetchContacts(mUri);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId()==R.id.action_save){
            boolean flag=save();
            if(flag==true) {
                finish();
            }else{
                Toast.makeText(EditContactActivity.this,"Please Check Firstly",Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean save(){
        updataCotact(mUri);
        return true;
    }


    public void updataCotact(Uri uri) {
        long  rawContactId = 0 ;
        ContentResolver cr = getContentResolver();
        Cursor people = cr.query(uri, new String[]{ContactsContract.Contacts.Data.RAW_CONTACT_ID}, null, null, null);
        if(people.getCount()>0){
            while (people.moveToNext()) {
                rawContactId = people.getLong(people.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
            }
        }

        Log.i("rawContactId", rawContactId+"");
//        RawContact rawContact = ContactManager.getRawContact(getApplicationContext(), rawContactId);
//        BatchOperation batchOperation = new BatchOperation(getApplicationContext(), getContentResolver());
//        rawContact.mFirstName = mFullNameView.getText().toString();
//        rawContact.mFullName = mFullNameView.getText().toString();
//        rawContact.mCellPhone = mCellPhoneView.getText().toString();
//        rawContact.mEmail = mEmailView.getText().toString();
//        Log.i("edit",rawContact.toString());
//        ContactManager.updateContactLocal(this, getContentResolver(), rawContact, rawContactId);


//        ContactManager.updateContact(getApplicationContext(),getContentResolver(),rawContact,false,false,false,true,rawContactId,batchOperation);

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

    public void fetchContacts(Uri uri) {
        ContentResolver cr = getContentResolver();
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
        mContactInfoTextView.setText(mContactInfoTextView.getText()+"  "+allContacts);
    }


    public void addOne(){
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

        //Log.i("Line43", Data.CONTENT_URI.toString()+" - "+rawContactInsertIndex);

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * Attempts to check input TODO check server permission or save priority in local preference
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no server checking is made.
     */
    private void checkInputAndPermission() {

        // Reset errors.
        mEmailView.setError(null);
        mCellPhoneView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mCellPhoneView.getText().toString();
        String cellphone = mCellPhoneView.getText().toString();
        String workphone = mWorkPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPhoneValid(password)) {
            mCellPhoneView.setError(getString(R.string.error_invalid_password));
            focusView = mCellPhoneView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isNameValid(String name){
        return false;
    }

    private boolean isPhoneValid(String phone){
        return false;
    }

}

