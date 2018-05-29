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

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.rest.new_rest.BaseApiManager;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.functions.Action1;

import static org.xwiki.android.authenticator.AppContext.getApiManager;

/**
 * SignInViewFlipper.
 */
public class SignInViewFlipper extends BaseViewFlipper {
    private static final String TAG = "SignInViewFlipper";

    private CharSequence accountName = null;
    private CharSequence accountPassword = null;

    private AsyncTask mAuthTask = null;

    public SignInViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
    }

    @Override
    public void doNext() {
        if (checkInput()) {
            mActivity.showProgress(mContext.getText(R.string.sign_in_authenticating), mAuthTask);
            submit();
        }
    }

    @Override
    public void doPrevious() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_IP);
    }

    private boolean checkInput() {
        EditText nameEditText = (EditText) findViewById(R.id.accountName);
        EditText passwordEditText = (EditText) findViewById(R.id.accountPassword);
        nameEditText.setError(null);
        passwordEditText.setError(null);
        accountName = nameEditText.getText();
        accountPassword = passwordEditText.getText();
        View focusView = null;
        boolean cancel = false;
        if (TextUtils.isEmpty(accountName)) {
            focusView = nameEditText;
            nameEditText.setError(mContext.getString(R.string.error_field_required));
            cancel = true;
        } else if (TextUtils.isEmpty(accountPassword) || accountPassword.length() < 5) {
            focusView = passwordEditText;
            passwordEditText.setError(mContext.getString(R.string.error_invalid_password));
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public void submit() {
        final String userName = accountName.toString();
        final String userPass = accountPassword.toString();

        BaseApiManager apiManager = getApiManager();

        String authtoken = XWikiHttp.login(
            userName,
            userPass
        );

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

    private Intent prepareIntent(String authtoken, String username, String password) {
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

    private void signedIn(String authtoken, String username, String password) {
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
                        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_SYNC);
                    }
                }
        );
    }

    private void showErrorMessage(String error){
        //Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
        final TextView errorTextView = (TextView) findViewById(R.id.error_msg);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(error);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                errorTextView.setVisibility(View.GONE);
            }
        }, 2000);
    }

}
