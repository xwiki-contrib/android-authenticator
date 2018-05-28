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
package org.xwiki.android.authenticator.activities.editcontact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.activities.base.BaseActivity;
import org.xwiki.android.authenticator.auth.XWikiAuthenticator;
import org.xwiki.android.authenticator.bean.UserPayload;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.contactdb.BatchOperation;
import org.xwiki.android.authenticator.contactdb.ContactManager;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Response;


/**
 * A Edit Contact Activity. You can modify your own information and
 * the administrator can modify all the users according the http response.
 */
public class EditContactActivity extends BaseActivity implements EditContactMvpView {

    private static final String TAG = EditContactActivity.class.getSimpleName();

    // UI references.
    @BindView(R.id.first_name)
    EditText mFirstNameView;

    @BindView(R.id.email)
    EditText mEmailView;

    @BindView(R.id.cell_phone)
    EditText mCellPhoneView;

    @BindView(R.id.last_name)
    EditText mLastNameView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    XWikiUser wikiUser = null;

    private EditContactPresenter editContactPresenter;
    private AtomicInteger atomicInteger;
    private AccountManager accountManager;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_edit_contact);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        atomicInteger = new AtomicInteger(0);
        editContactPresenter = new EditContactPresenter(this);
        editContactPresenter.attachView(this);
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));

        Uri mUri = getIntent().getData();
        wikiUser = getXWikiUser(this, mUri);
        if (wikiUser == null) {
            finish();
            return;
        }

        mEmailView.setText(wikiUser.getEmail());
        mFirstNameView.setText(wikiUser.getFirstName());
        mCellPhoneView.setText(wikiUser.getPhone());
        mLastNameView.setText(wikiUser.getLastName());
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
                updateContact();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private XWikiUser getXWikiUser(Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);
        //getRawContactId
        long rawContactId = 0;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToNext()) {
                rawContactId = cursor.getLong(
                        cursor.getColumnIndex(ContactsContract.Contacts.Data.RAW_CONTACT_ID));
            }
            cursor.close();
        }
        //getXWikiUser
        if (rawContactId > 0) {
            //first, lastName, email, phone, serverId=id.
            return ContactManager.getXWikiUser(context, rawContactId);
        }
        return null;
    }

    public void updateContact() {
        String[] idArray = XWikiUser.splitId(wikiUser.getId());
        UserPayload userPayload = new UserPayload();
        userPayload.setClassName("XWiki.XWikiUsers");
        userPayload.setFirstName(wikiUser.firstName);
        userPayload.setEmail(wikiUser.email);
        userPayload.setLastName(wikiUser.lastName);
        userPayload.setPhone(wikiUser.phone);

        assert idArray != null;
        editContactPresenter.updateUser(idArray[0], idArray[1], idArray[2], userPayload);
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

    @Override
    public void showProgress() {
        showProgressDialog();
        onProgressDialogCancel();
    }

    @Override
    public void hideProgress() {
        hideProgressDialog();
    }

    @Override
    public void showContactUpdateSuccessfully() {
        //update local
        BatchOperation batchOperation = new BatchOperation(EditContactActivity.this,
                getContentResolver());
        //TODO:: URGENT!!! UPDATE USER CONTACT
//        ContactManager.updateContact(EditContactActivity.this, getContentResolver(),
//                wikiUser, false, false, false, true, wikiUser.rawId, batchOperation);
        batchOperation.execute();
        showToast("Update Successfully.");
        finish();
    }

    @Override
    public void showErrorOnUpdatingContact() {
        if (atomicInteger.intValue() == 0) {
            atomicInteger.incrementAndGet();
            accountManager = AccountManager.get(getApplicationContext());
            Account availableAccounts[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
            account = availableAccounts[0];
            String accountPassword = accountManager.getUserData(account,
                    AccountManager.KEY_PASSWORD);
            editContactPresenter.login(account.name, accountPassword);
            SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(),
                    Constants.COOKIE, null);
        } else {
            showToast("You have no permission !!!");
        }
    }

    @Override
    public void showLoginSuccessfully(Response<ResponseBody> responseBody) {
        String authToken = responseBody.headers().get("Set-Cookie");
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(),
                Constants.COOKIE, authToken);
        XWikiAuthenticator.refreshAllAuthTokenType(accountManager, account, authToken);
        updateContact();
    }

    @Override
    public void showErrorLogin() {
        showToast("Authentication Error");
    }

    @Override
    public void onProgressDialogCancel() {
        getProgressDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                editContactPresenter.clearSubscription();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editContactPresenter.detachView();
    }
}

