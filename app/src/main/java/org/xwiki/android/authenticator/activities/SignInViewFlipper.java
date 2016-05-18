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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.rest.HttpResponse;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

/**
 * Created by lf on 2016/5/16.
 */
public class SignInViewFlipper extends BaseViewFlipper{
    private static final String TAG = "SignInViewFlipper";

    private CharSequence accountName = null;
    private CharSequence accountPassword = null;

    public SignInViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
    }

    @Override
    public void doNext() {
        if(checkInput()){
            submit();
        }
    }

    @Override
    public void doPrevious() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_IP);
    }

    private boolean checkInput(){
        EditText nameEditText = (EditText) findViewById(R.id.accountName);
        EditText passwordEditText = (EditText) findViewById(R.id.accountPassword);
        nameEditText.setError(null);
        passwordEditText.setError(null);
        accountName = nameEditText.getText();
        accountPassword = passwordEditText.getText();
        View focusView = null;
        boolean cancel = false;
        if(TextUtils.isEmpty(accountName)){
            focusView = nameEditText;
            nameEditText.setError(mContext.getString(R.string.error_field_required));
            cancel = true;
        }else if(TextUtils.isEmpty(accountPassword)){
            focusView = passwordEditText;
            passwordEditText.setError(mContext.getString(R.string.error_field_required));
            cancel = true;
        }
        if(cancel) {
            focusView.requestFocus();
            return false;
        }else {
            return true;
        }
    }

    public void submit() {
        final String userServer = SharedPrefsUtils.getValue(mContext, Constants.SERVER_ADDRESS, null);
        final String userName = accountName.toString();
        final String userPass = accountPassword.toString();

        final String accountType = mActivity.getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        new AsyncTask<String, Void, Intent>() {
            @Override
            protected Intent doInBackground(String... params) {
                Log.d(TAG, "Started authenticating");
                Bundle data = new Bundle();
                try {
                    Log.d(TAG, userName + " " + userPass + " " + userServer);
                    HttpResponse response = XWikiHttp.login(userServer, userName, userPass);
                    Log.d(TAG, response.getHeaders().toString() + response.getResponseCode());
                    int statusCode = response.getResponseCode();
                    if (statusCode < 200 || statusCode > 299) {
                        String msg = "statusCode=" + statusCode + ", response=" + response.getResponseMessage();
                        data.putString(AuthenticatorActivity.KEY_ERROR_MESSAGE, msg);
                    } else {
                        String authtoken = response.getHeaders().get("Set-Cookie");
                        data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                        data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                        data.putString(AuthenticatorActivity.PARAM_USER_SERVER, userServer);
                        data.putString(AuthenticatorActivity.PARAM_USER_PASS, userPass);
                    }
                } catch (Exception e) {
                    data.putString(AuthenticatorActivity.KEY_ERROR_MESSAGE, e.toString());
                }
                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent.hasExtra(AuthenticatorActivity.KEY_ERROR_MESSAGE)) {
                    Toast.makeText(mContext, intent.getStringExtra(AuthenticatorActivity.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                } else {
                    mActivity.finishLogin(intent);
                    mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_SYNC);
                }
            }
        }.execute();
    }

}
