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
package org.xwiki.android.sync.activities.editcontact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.activities.base.BaseActivity;
import org.xwiki.android.sync.auth.XWikiAuthenticator;
import org.xwiki.android.sync.bean.UserPayload;
import org.xwiki.android.sync.bean.XWikiUser;
import org.xwiki.android.sync.contactdb.BatchOperation;
import org.xwiki.android.sync.contactdb.ContactManager;
import org.xwiki.android.sync.utils.SharedPrefsUtils;
import org.xwiki.android.sync.utils.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Response;


/**
 * A Edit Contact Activity. You can modify your own information and
 * the administrator can modify all the users according the http response.
 *
 * @version $Id$
 */
public class EditContactActivity extends BaseActivity implements EditContactMvpView {

    /**
     * Tag which will be used for logging.
     */
    private static final String TAG = EditContactActivity.class.getSimpleName();

    /**
     * {@link EditText} for first name.
     */
    private EditText mFirstNameView;

    /**
     * {@link EditText} for email.
     */
    private EditText mEmailView;

    /**
     * {@link EditText} for cell phone.
     */
    private EditText mCellPhoneView;

    /**
     * {@link EditText} for last name.
     */
    private EditText mLastNameView;

    /**
     * User which will be edited.
     */
    private XWikiUser wikiUser = null;

    /**
     * Presenter.
     */
    private EditContactPresenter editContactPresenter;

    private AtomicInteger atomicInteger;

    /**
     * Android account manager.
     */
    private AccountManager accountManager;

    /**
     * User account which will be used for operations with contact edition.
     */
    private Account account;

    /**
     * Init variables, set click handlers.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}. <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_edit_contact);

        mFirstNameView = findViewById(R.id.first_name);
        mLastNameView = findViewById(R.id.last_name);
        mEmailView = findViewById(R.id.email);
        mCellPhoneView = findViewById(R.id.cell_phone);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        atomicInteger = new AtomicInteger(0);
        editContactPresenter = new EditContactPresenter(this);
        editContactPresenter.attachView(this);

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

        findViewById(R.id.action_save).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkInput()) {
                        updateContact();
                    }
                }
            }
        );
    }

    /**
     * @param context Context for getting {@link ContentResolver}
     * @param uri Uri to find the user
     * @return user which was found or null
     */
    @Nullable
    private static XWikiUser getXWikiUser(Context context, Uri uri) {
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

    /**
     * Init update user procedure.
     */
    private void updateContact() {
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
     * Attempts to check input.
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

    /**
     * Should be called when a time taking process starts and we want the user
     * to wait for the process to finish. The UI should gracefully display some
     * sort of progress bar or animation so that the user knows that the app is
     * doing some work and has not stalled.
     *
     * <p>For example: a network request to the API is made for authenticating
     * the user.</p>
     */
    @Override
    public void showProgress() {
        showProgressDialog();
        initOnProgressDialogCancel();
    }

    /**
     * Should be called when a time taking process ends and we have some result
     * for the user.
     */
    @Override
    public void hideProgress() {
        hideProgressDialog();
    }

    /**
     * Show to user that contact was successfully updated
     */
    @Override
    public void showContactUpdateSuccessfully() {
        //update local
        BatchOperation batchOperation = new BatchOperation(getContentResolver());
        //TODO:: URGENT!!! UPDATE USER CONTACT
//        ContactManager.updateContact(EditContactActivity.this, getContentResolver(),
//                wikiUser, false, false, false, true, wikiUser.rawId, batchOperation);
        batchOperation.execute();
        showToast(getString(R.string.updateSuccess));
        finish();
    }

    /**
     * Show to user that contact was not updated for the some reason.
     */
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
            showToast(getString(R.string.haveNoPermission));
        }
    }

    /**
     * Actually update cookie and restart update contact.
     *
     * @param responseBody Body which must contains new cookies
     */
    @Override
    public void showLoginSuccessfully(Response<ResponseBody> responseBody) {
        String authToken = responseBody.headers().get("Set-Cookie");
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(),
                Constants.COOKIE, authToken);
        XWikiAuthenticator.refreshAllAuthTokenType(accountManager, account, authToken);
        updateContact();
    }

    /**
     * Show to user that authorisation was failed.
     */
    @Override
    public void showErrorLogin() {
        showToast(getString(R.string.authenticationError));
    }

    /**
     * Call this method for init progress dialog cancel listener.
     */
    @Override
    public void initOnProgressDialogCancel() {
        getProgressDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                editContactPresenter.clearSubscription();
            }
        });
    }

    /**
     * Detach presenter.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        editContactPresenter.detachView();
    }
}

