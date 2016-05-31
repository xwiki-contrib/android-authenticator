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
package org.xwiki.android.authenticator.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.XWikiAuthenticator;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.contactdb.BatchOperation;
import org.xwiki.android.authenticator.contactdb.ContactManager;
import org.xwiki.android.authenticator.rest.HttpResponse;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.io.IOException;


/**
 * A Edit Contact Activity. You can modify your own information and
 * the administrator can modify all the users according the http response.
 */
public class EditContactActivity extends AppCompatActivity {
    private static final String TAG = "EditContactActivity";

    // UI references.
    private EditText mFirstNameView;
    private EditText mEmailView;
    private EditText mCellPhoneView;
    private EditText mLastNameView;

    XWikiUser wikiUser = null;

    private AsyncTask mUpdateUserTask;
    //show progress dialog
    private Dialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_edit_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mCellPhoneView = (EditText) findViewById(R.id.cell_phone);
        mLastNameView = (EditText) findViewById(R.id.last_name);

        Uri mUri = getIntent().getData();
        wikiUser = getXWikiUser(this, mUri);
        if (wikiUser == null) {
            finish();
            return;
        }

        if (wikiUser != null) {
            mEmailView.setText(wikiUser.getEmail());
            mFirstNameView.setText(wikiUser.getFirstName());
            mCellPhoneView.setText(wikiUser.getPhone());
            mLastNameView.setText(wikiUser.getLastName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_save) {
            //check input valid  set input value (firstName, lastName, email, cellPhone)
            if (checkInput()) {
                updataContact();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mUpdateUserTask != null && !mUpdateUserTask.isCancelled()){
            mUpdateUserTask.cancel(true);
        }
        mUpdateUserTask = null;
    }

    private XWikiUser getXWikiUser(Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);
        //getRawContactId
        long rawContactId = 0;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                rawContactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.Data.RAW_CONTACT_ID));
                break;
            }
        }
        cursor.close();
        //getXWikiUser
        if (rawContactId > 0) {
            //first, lastName, email, phone, serverId=id.
            return ContactManager.getXWikiUser(context, rawContactId);
        }
        return null;
    }




    public void updataContact() {
        //show progress
        showProgress("Update user...", mUpdateUserTask);
        //update server at first and check if the user has permission to modify
        // according the response.
        mUpdateUserTask = new AsyncTask<Void, Void, HttpResponse>() {
            @Override
            protected HttpResponse doInBackground(Void... params) {
                try {
                    XWikiUser updateUser = new XWikiUser();
                    String[] idArray = XWikiUser.splitId(wikiUser.getId());
                    updateUser.wiki = idArray[0];
                    updateUser.space = idArray[1];
                    updateUser.pageName = idArray[2];
                    updateUser.firstName = wikiUser.firstName;
                    updateUser.lastName = wikiUser.lastName;
                    updateUser.email = wikiUser.email;
                    updateUser.phone = wikiUser.phone;
                    HttpResponse response = XWikiHttp.updateUser(updateUser);
                    if(response.getResponseCode() == 401){
                        //login to set new cookies
                        AccountManager mAccountManager = AccountManager.get(getApplicationContext());
                        Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
                        Account account = availableAccounts[0];
                        String accountPassword = mAccountManager.getUserData(account, AccountManager.KEY_PASSWORD);
                        HttpResponse httpResponse = XWikiHttp.login(XWikiHttp.getServerAddress(), account.name, accountPassword);
                        int statusCode = httpResponse.getResponseCode();
                        if (statusCode >= 200 && statusCode <= 299) {
                            String authToken = httpResponse.getHeaders().get("Set-Cookie");
                            XWikiAuthenticator.refreshAllAuthTokenType(mAccountManager, account, authToken);
                            response = XWikiHttp.updateUser(updateUser);
                        }
                    }
                    return response;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(HttpResponse response) {
                hideProgress();
                if (response == null) {
                    Toast.makeText(EditContactActivity.this, "Network Error !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //500:no permission,  200:success
                int statusCode = response.getResponseCode();
                if (statusCode == 401) {
                    Toast.makeText(EditContactActivity.this, "You have no permission !!!", Toast.LENGTH_SHORT).show();
                } else if(statusCode < 200 || statusCode > 299) {
                    Toast.makeText(EditContactActivity.this, "error:"+response.getResponseMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    //update local
                    BatchOperation batchOperation = new BatchOperation(EditContactActivity.this, getContentResolver());
                    ContactManager.updateContact(EditContactActivity.this, getContentResolver(), wikiUser, false, false, false, true, wikiUser.rawId, batchOperation);
                    batchOperation.execute();
                    Toast.makeText(EditContactActivity.this, "Update Successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }.execute();
    }

    /**
     * Attempts to check input
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no server checking is made.
     */
    private boolean checkInput() {
        // Reset errors.
        mEmailView.setError(null);
        mCellPhoneView.setError(null);
        mLastNameView.setError(null);
        mFirstNameView.setError(null);

        // Store values at the time of the login attempt.
        wikiUser.email = mEmailView.getText().toString();
        wikiUser.phone = mCellPhoneView.getText().toString();
        wikiUser.firstName = mFirstNameView.getText().toString();
        wikiUser.lastName = mLastNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(wikiUser.firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(wikiUser.lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(wikiUser.phone)) {
            wikiUser.phone = "";
        } else if (!StringUtils.isEmail(wikiUser.email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            // perform the user login attempt.
            return true;
        }
    }

    public void showProgress(CharSequence message, final AsyncTask asyncTask) {
        // To avoid repeatedly create
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "user cancelling authentication");
                if (asyncTask != null) {
                    asyncTask.cancel(true);
                }
            }
        });
        // We save off the progress dialog in a field so that we can dismiss
        // it later.
        mProgressDialog = dialog;
        mProgressDialog.show();
    }

    /**
     * Hides the progress UI for a lengthy operation.
     */
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}

