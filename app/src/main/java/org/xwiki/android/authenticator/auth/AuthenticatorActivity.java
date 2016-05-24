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
package org.xwiki.android.authenticator.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.activities.SettingSyncViewFlipper;
import org.xwiki.android.authenticator.activities.SettingIpViewFlipper;
import org.xwiki.android.authenticator.activities.SignInViewFlipper;
import org.xwiki.android.authenticator.activities.SignUpStep1ViewFlipper;
import org.xwiki.android.authenticator.activities.SignUpStep2ViewFlipper;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @version $Id: $
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "AuthenticatorActivity";

    public static final String KEY_AUTH_TOKEN_TYPE = "KEY_AUTH_TOKEN_TYPE";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public final static String PARAM_USER_SERVER = "XWIKI_USER_SERVER";
    public final static String PARAM_USER_PASS = "XWIKI_USER_PASS";
    public final static String PARAM_APP_UID = "PARAM_APP_UID";
    public final static String PARAM_APP_PACKAGENAME = "PARAM_APP_PACKAGENAME";
    public final static String IS_SETTING_SYNC_TYPE = "IS_SETTING_SYNC_TYPE";

    private SettingIpViewFlipper settingsIpViewFlipper;
    private SignInViewFlipper signInViewFlipper;
    private SettingSyncViewFlipper settingSyncViewFlipper;
    private SignUpStep1ViewFlipper signUpStep1ViewFlipper;
    private SignUpStep2ViewFlipper signUpStep2ViewFlipper;

    private AccountManager mAccountManager;

    private ViewFlipper mViewFlipper;
    private Toolbar toolbar;
    //show refresh
    public SwipeRefreshLayout swipeRefreshLayout;
    public ImageView refreshImageView;
    //show progress dialog
    private Dialog mProgressDialog = null;
    //add all asyncTask and clear all tasks when calling showViewFlipper and onDestroy
    private List<AsyncTask<Void, Void, Object>> mAsyncTasks = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_authenticator);
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("XWiki Account");

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.swipeRefreshColors));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);

        refreshImageView = (ImageView) findViewById(R.id.refresh_view);

        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        boolean is_set_sync = getIntent().getBooleanExtra(AuthenticatorActivity.IS_SETTING_SYNC_TYPE, true);
        if (is_set_sync) {
            //just set sync
            showViewFlipper(ViewFlipperLayoutId.SETTING_SYNC);
        } else {
            //add new contact
            //check if there'is already a user, finish and return, keep only one user.
            mAccountManager = AccountManager.get(getApplicationContext());
            Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
            if (availableAccounts.length > 0) {
                Toast.makeText(this, "The user already exists!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.act_authenticator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearAsyncTask();
    }

    private void doPreviousNext(boolean next) {
        int id = mViewFlipper.getDisplayedChild();
        switch (id) {
            case ViewFlipperLayoutId.SETTING_IP:
                if (settingsIpViewFlipper == null) {
                    settingsIpViewFlipper = new SettingIpViewFlipper(this, mViewFlipper.getChildAt(id));
                }
                if (next) {
                    settingsIpViewFlipper.doNext();
                } else {
                    settingsIpViewFlipper.doPrevious();
                }
                break;
            case ViewFlipperLayoutId.SIGN_IN:
                if (next) {
                    signInViewFlipper.doNext();
                } else {
                    signInViewFlipper.doPrevious();
                }
                break;
            case ViewFlipperLayoutId.SETTING_SYNC:
                if (next) {
                    settingSyncViewFlipper.doNext();
                } else {
                    settingSyncViewFlipper.doPrevious();
                }
                break;
            case ViewFlipperLayoutId.SIGN_UP_STEP1:
                if (next) {
                    signUpStep1ViewFlipper.doNext();
                } else {
                    signUpStep1ViewFlipper.doPrevious();
                }
                break;
            case ViewFlipperLayoutId.SIGN_UP_STEP2:
                if (next) {
                    signUpStep2ViewFlipper.doNext();
                } else {
                    signUpStep2ViewFlipper.doPrevious();
                }
                break;
            default:
                break;
        }
    }

    public void doPrevious(View view) {
        doPreviousNext(false);
    }

    public void doNext(View view) {
        doPreviousNext(true);
    }

    public void setLeftRightButton(String leftButton, String rightButton) {
        ((Button) findViewById(R.id.left_button)).setText(leftButton);
        ((Button) findViewById(R.id.right_button)).setText(rightButton);
    }

    @Override
    public void onRefresh() {
        int id = mViewFlipper.getDisplayedChild();
        switch (id) {
            case ViewFlipperLayoutId.SIGN_UP_STEP2:
                signUpStep2ViewFlipper.onRefresh();
                break;
            case ViewFlipperLayoutId.SETTING_SYNC:
                settingSyncViewFlipper.onRefresh();
                break;
            default:
                break;
        }
    }

    public interface ViewFlipperLayoutId {
        int SETTING_IP = 0;
        int SIGN_IN = 1;
        int SETTING_SYNC = 2;
        int SIGN_UP_STEP1 = 3;
        int SIGN_UP_STEP2 = 4;
    }

    public void showViewFlipper(int id) {
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
        clearAsyncTask();
        swipeRefreshLayout.setEnabled(false);
        refreshImageView.setVisibility(View.GONE);
        mViewFlipper.setDisplayedChild(id);
        switch (id) {
            case ViewFlipperLayoutId.SETTING_IP:
                if (settingsIpViewFlipper == null) {
                    settingsIpViewFlipper = new SettingIpViewFlipper(this, mViewFlipper.getChildAt(id));
                }
                toolbar.setTitle("XWiki Account");
                setLeftRightButton("Sign In", "Sign Up");
                break;
            case ViewFlipperLayoutId.SIGN_IN:
                if (signInViewFlipper == null) {
                    signInViewFlipper = new SignInViewFlipper(this, mViewFlipper.getChildAt(id));
                }
                toolbar.setTitle("Sign In");
                setLeftRightButton("Previous", "Login");
                break;
            case ViewFlipperLayoutId.SETTING_SYNC:
                refreshImageView.setVisibility(View.VISIBLE);
                if (settingSyncViewFlipper == null) {
                    settingSyncViewFlipper = new SettingSyncViewFlipper(this, mViewFlipper.getChildAt(id));
                }
                toolbar.setTitle("Setting Sync");
                setLeftRightButton("Don't Sync", "Complete");
                break;
            case ViewFlipperLayoutId.SIGN_UP_STEP1:
                if (signUpStep1ViewFlipper == null) {
                    signUpStep1ViewFlipper = new SignUpStep1ViewFlipper(this, mViewFlipper.getChildAt(id));
                }
                toolbar.setTitle("Sign Up Step1");
                setLeftRightButton("Previous", "Next");
                break;
            case ViewFlipperLayoutId.SIGN_UP_STEP2:
                swipeRefreshLayout.setEnabled(true);
                if (signUpStep2ViewFlipper == null) {
                    signUpStep2ViewFlipper = new SignUpStep2ViewFlipper(this, mViewFlipper.getChildAt(id));
                }
                toolbar.setTitle("Sign Up Step2");
                setLeftRightButton("Previous", "Register");
                break;
        }
    }

    public String[] getStep1Values() {
        return signUpStep1ViewFlipper.getValues();
    }

    public void finishLogin(Intent intent) {
        Log.d(TAG, "> finishLogin");

        //set rest url null (Maybe also need to do somethings to clear old value)
        //because when you remove the account and add again, the static XWiki.serverRestPreUrl is not updated. serverAddr is the old address.
        XWikiHttp.setRestUrlNULL();

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        String accountServer = intent.getStringExtra(PARAM_USER_SERVER);
        final Account account = new Account(accountName, Constants.ACCOUNT_TYPE);

        Log.d(TAG, "finishLogin > addAccountExplicitly" + " " + intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        Bundle data = new Bundle();
        data.putString(PARAM_USER_SERVER, accountServer);
        data.putString(PARAM_USER_PASS, accountPassword);

        // Creating the account on the device and setting the auth token we got
        // (Not setting the auth token will cause another call to the server to authenticate the user)
        mAccountManager.addAccountExplicitly(account, accountPassword, data);

        mAccountManager.setUserData(account, AccountManager.KEY_USERDATA, accountName);
        mAccountManager.setUserData(account, AccountManager.KEY_PASSWORD, accountPassword);
        mAccountManager.setUserData(account, AuthenticatorActivity.PARAM_USER_SERVER, accountServer);

        //clear all SharedPreferences
        //SharedPrefsUtil.clearAll(AuthenticatorActivity.this);

        //grant permission if adding user from the third-party app (UID,PackageName);
        String packaName = getIntent().getStringExtra(PARAM_APP_PACKAGENAME);
        int uid = getIntent().getIntExtra(PARAM_APP_UID, 0);
        Log.d(TAG, packaName + ", " + getPackageName());
        //only if adding account from the third-party apps exclude android.uid.system, this will execute to grant permission and set token
        if (!packaName.contains("android.uid.system")) {
            AppContext.addAuthorizedApp(uid, packaName);
            String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            if (!TextUtils.isEmpty(authToken)) {
                String authTokenType = getIntent().getStringExtra(KEY_AUTH_TOKEN_TYPE);
                mAccountManager.setAuthToken(account, authTokenType, authToken);
            }
        }

        //set sync
        //ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
        //ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);

        //Bundle params = new Bundle();
        //params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
        //params.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false);
        //params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        //ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, params, 150);
        //ContentResolver.requestSync(account,ContactsContract.AUTHORITY,params);

        //return value to AccountManager
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        Log.d(TAG, ">" + "finish return");
        // in SettingSyncViewFlipper finish;
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

    public void putAsyncTask(AsyncTask<Void, Void, Object> asyncTask) {
        mAsyncTasks.add(asyncTask.execute());
    }

    private void clearAsyncTask() {
        Iterator<AsyncTask<Void, Void, Object>> iterator = mAsyncTasks.iterator();
        while (iterator.hasNext()) {
            AsyncTask<Void, Void, Object> asyncTask = iterator.next();
            if (asyncTask != null && !asyncTask.isCancelled()) {
                asyncTask.cancel(true);
            }
        }
        mAsyncTasks.clear();
    }
}
