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
package org.xwiki.android.sync.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.auth.AuthenticatorActivity;
import org.xwiki.android.sync.rest.XWikiHttp;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Main auth flipper.
 *
 * @version $Id$
 */
public class SignInViewFlipper extends BaseViewFlipper {

    /**
     * Tag for logging.
     */
    private static final String TAG = "SignInViewFlipper";

    /**
     * Typed username.
     */
    private String accountName = null;

    /**
     * Typed password.
     */
    private String accountPassword = null;

    /**
     * Standard constructor
     *
     * @param activity Current activity
     * @param contentRootView Root view of this flipper
     */
    public SignInViewFlipper(
        @NonNull AuthenticatorActivity activity,
        @NonNull View contentRootView
    ) {
        super(activity, contentRootView);
        findViewById(R.id.signInButton).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkInput()) {
                        mActivity.showProgress(
                            mContext.getText(R.string.sign_in_authenticating),
                            submit()
                        );
                    }
                }
            }
        );
    }

    /**
     * Calling when user push "login".
     */
    @Override
    public void doNext() { }

    /**
     * Return to setting server ip address, calling by pressing "back".
     */
    @Override
    public void doPrevious() { }

    /**
     * @return true if current input correct and variables {@link #accountName} and
     * {@link #accountPassword} was correctly set
     */
    private boolean checkInput() {
        EditText nameEditText = findViewById(R.id.accountName);
        EditText passwordEditText = findViewById(R.id.accountPassword);
        nameEditText.setError(null);
        passwordEditText.setError(null);
        accountName = nameEditText.getText().toString();
        accountPassword = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(accountName)) {
            nameEditText.requestFocus();
            nameEditText.setError(mContext.getString(R.string.error_field_required));
            return false;
        } else if (TextUtils.isEmpty(accountPassword) || accountPassword.length() < 5) {
            passwordEditText.requestFocus();
            passwordEditText.setError(mContext.getString(R.string.error_invalid_password));
            return false;
        }
        return true;
    }

    /**
     * Start login procedure.
     *
     * @return Subscription which can be unsubscribed for preventing log in if user cancel it
     */
    private Subscription submit() {
        final String userName = accountName;
        final String userPass = accountPassword;

        return XWikiHttp.login(
            userName,
            userPass
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                new Action1<String>() {
                    @Override
                    public void call(String authtoken) {
                        mActivity.hideProgress();
                        if (authtoken == null) {
                            showErrorMessage(mContext.getString(R.string.loginError));
                        } else {
                            signedIn(
                                authtoken,
                                userName,
                                userPass
                            );
                        }
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mActivity.hideProgress();
                        showErrorMessage(mContext.getString(R.string.loginError));
                    }
                }
            );
    }

    /**
     * Prepare log in intent which will contains credentials and other data.
     *
     * @param authtoken Authtoken (or session cookie) which was set by response
     * @param username Account username to save
     * @param password Account password to save
     * @return Prepared intent
     *
     * @since 0.4
     */
    private Intent prepareIntent(
        @NonNull String authtoken,
        @NonNull String username,
        @NonNull String password
    ) {
        String userServer = SharedPrefsUtils.getValue(mContext, Constants.SERVER_ADDRESS, null);

        String accountType = mActivity.getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
        data.putString(AuthenticatorActivity.PARAM_USER_SERVER, userServer);
        data.putString(AuthenticatorActivity.PARAM_USER_PASS, password);

        final Intent intent = new Intent();
        intent.putExtras(data);
        return intent;
    }

    /**
     * Must be called if user successfully logged in.
     *
     * @param authtoken Authtoken (or session cookie) which was set by response
     * @param username Account username to save
     * @param password Account password to save
     *
     * @since 0.4
     */
    private void signedIn(
        @NonNull String authtoken,
        @NonNull String username,
        @NonNull String password
    ) {
        final Intent signedIn = prepareIntent(
                authtoken,
                username,
                password
        );

        mActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        mActivity.hideProgress();
                        mActivity.finishLogin(signedIn);
                        mActivity.hideInputMethod();
                        mActivity.doNext(mContentRootView);
                    }
                }
        );
    }

    /**
     * Must be called to show user that something went wrong.
     *
     * @param error String which must be shown in error message
     */
    private void showErrorMessage(String error){
        final TextView errorTextView = findViewById(R.id.error_msg);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(error);
        new Handler().postDelayed(
            new Runnable() {
                @Override
                public void run() {
                    errorTextView.setVisibility(View.GONE);
                }
            },
            2000
        );
    }

}
