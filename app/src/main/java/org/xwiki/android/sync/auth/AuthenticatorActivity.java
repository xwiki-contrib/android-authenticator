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
package org.xwiki.android.sync.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.activities.BaseViewFlipper;
import org.xwiki.android.sync.activities.SettingServerIpViewFlipper;
import org.xwiki.android.sync.activities.SignInViewFlipper;
import org.xwiki.android.sync.activities.SyncSettingsActivity;
import org.xwiki.android.sync.utils.IntentUtils;
import org.xwiki.android.sync.utils.PermissionsUtils;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

/**
 * Most important activity in authorisation process
 *
 * @version $Id$
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    /**
     * Contains order of flippers in authorisation progress.
     *
     * <p>
     *     All flippers must support constructor
     *     {@link BaseViewFlipper#BaseViewFlipper(AuthenticatorActivity, View)}, because
     *     all instances will be created automatically using reflection in
     *     {@link #showViewFlipper(int)}
     * </p>
     */
    private static final List<Class<? extends BaseViewFlipper>> orderOfFlippers;

    static {
        orderOfFlippers = new ArrayList<>();
        orderOfFlippers.add(SettingServerIpViewFlipper.class);
        orderOfFlippers.add(SignInViewFlipper.class);
    }

    /**
     * Tag which will be used for logging
     */
    private static final String TAG = "AuthenticatorActivity";

    public static final String KEY_AUTH_TOKEN_TYPE = "KEY_AUTH_TOKEN_TYPE";
    public static final String PARAM_USER_SERVER = "XWIKI_USER_SERVER";
    public static final String PARAM_USER_PASS = "XWIKI_USER_PASS";
    public static final String PARAM_APP_UID = "PARAM_APP_UID";
    public static final String PARAM_APP_PACKAGENAME = "PARAM_APP_PACKAGENAME";
    public static final String IS_SETTING_SYNC_TYPE = "IS_SETTING_SYNC_TYPE";

    /**
     * List of flippers or nulls.
     * <p>
     *     Danger, can contains nulls, not recommended to use it directly
     * </p>
     *
     * @see #showViewFlipper(int)
     */
    private final List<BaseViewFlipper> flippers = new ArrayList<>();

    /**
     * Will be used for managing of user account.
     */
    private AccountManager mAccountManager;

    /**
     * Flippers root.
     */
    private ViewFlipper mViewFlipper;

    /**
     * Toolbar of current activity.
     */
    private Toolbar toolbar;

    /**
     * Current progress dialog.
     */
    private Dialog mProgressDialog = null;

    /**
     * Code which await to returns for requesting permissions
     */
    private static final int REQUEST_PERMISSIONS_CODE = 1;

    /**
     * <ol>
     *     <li>Init view</li>
     *     <li>Init {@link #toolbar}</li>
     *     <li>Init {@link #mViewFlipper}</li>
     *     <li>Init action (settings or full auth)</li>
     * </ol>
     *
     * @param savedInstanceState Used by default
     * @see Activity#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_authenticator);

        PermissionsUtils permissionsUtils = new PermissionsUtils(this);
        if (!permissionsUtils.checkPermissions()) {
            permissionsUtils.requestPermissions(REQUEST_PERMISSIONS_CODE);
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.xwikiAccount);

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            builder = new AlertDialog.Builder(AuthenticatorActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(AuthenticatorActivity.this);
        }
        builder.setTitle(R.string.xwiki)
                .setIcon(getResources().getDrawable(R.drawable.logo))
                .setMessage(R.string.signUpOfferMessage)
                .setPositiveButton(
                    android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
                );
        mViewFlipper = findViewById(R.id.view_flipper);
        Integer position;
        mAccountManager = AccountManager.get(getApplicationContext());
        Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.Companion.getACCOUNT_TYPE());
        position = 0;
        if (availableAccounts.length > 0) {
            Toast.makeText(this, "The user already exists!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        showViewFlipper(position);
    }

    /**
     * Intercept back button pressing and remap it to flipper if current flipper child is
     * not settings.
     */
    @Override
    public void onBackPressed() {
        if(mViewFlipper.getDisplayedChild() == orderOfFlippers.indexOf(SettingServerIpViewFlipper.class)) {
            super.onBackPressed();
        } else {
            doPrevious(
                mViewFlipper.getCurrentView()
            );
        }
    }

    /**
     * Must be called when current flipper must receive calling of
     * {@link BaseViewFlipper#doPrevious()} and be changed to previous.
     *
     * @param view View, which trigger action
     */
    public void doPrevious(View view) {
        Integer position = mViewFlipper.getDisplayedChild();
        chooseAnimation(position == orderOfFlippers.indexOf(SettingServerIpViewFlipper.class));
        flippers.get(
            position
        ).doPrevious();
        showViewFlipper(position - 1);
    }

    /**
     * Must be called when current flipper must receive calling of
     * {@link BaseViewFlipper#doNext()} and be changed to next.
     *
     * @param view View, which trigger action
     */
    public void doNext(View view) {
        Integer position = mViewFlipper.getDisplayedChild();
        chooseAnimation(true);
        flippers.get(
            position
        ).doNext();
        if (position + 1 >= orderOfFlippers.size()) {
            finish();
        }
        showViewFlipper(position + 1);
    }

    /**
     * Util method, choose animation in dependency to toNext.
     *
     * @param toNext If true - will be used animation right-to-left (<-),
     *               left-to-right otherwise (->)
     *
     * @since 0.4.2
     */
    private void chooseAnimation(@NonNull Boolean toNext) {
        if (toNext) {
            mViewFlipper.setInAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.push_left_in
                )
            );
            mViewFlipper.setOutAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.push_left_out
                )
            );
        } else {
            mViewFlipper.setInAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.push_right_in
                )
            );
            mViewFlipper.setOutAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.push_right_out
                )
            );
        }
    }

    //TODO:: Replace by normal registration
    /**
     * Will be called when user push to "Create one" button.
     *
     * @param view View which trigger action
     */
    public void signUp(View view) {
        String url = AppContext.Companion.currentBaseUrl();
        if (url.endsWith("/")) {
            url += "bin/view/XWiki/Registration";
        } else {
            url += "/bin/view/XWiki/Registration";
        }
        Intent intent = IntentUtils.Companion.openLink(
            url
        );
        startActivity(intent);
    }

    /**
     * Set current visible element and init flipper if it needed.
     *
     * <p>
     *     Procedure of initialisation:
     * </p>
     *
     * <ol>
     *     <li> Get class of flipper </li>
     *     <li> Get constructor which signature duplicate
     *          {@link BaseViewFlipper#BaseViewFlipper(AuthenticatorActivity, View)}
     *     </li>
     *     <li> Create instance using "this" and {@link #mViewFlipper}</li>
     * </ol>
     *
     * @param position Position of item which must be shown
     *
     * @since 0.4.2
     */
    public void showViewFlipper(int position) {
        mViewFlipper.setDisplayedChild(position);
        while (flippers.size() <= position) {
            flippers.add(null);
        }
        BaseViewFlipper flipper = flippers.get(position);
        if (flipper == null) {
            try {
                flipper = orderOfFlippers.get(
                    position
                ).getConstructor(
                    AuthenticatorActivity.class,
                    View.class
                ).newInstance(
                    this,
                    mViewFlipper.getChildAt(position)
                );
                flippers.set(
                    position,
                    flipper
                );
            } catch (InstantiationException e) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e);
            } catch (InvocationTargetException e) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e);
            } finally {
                if (flipper == null) {
                    return;
                }
            }
        }
        String title = flipper.getTitle();
        if (title == null) {
            title = getString(R.string.app_name);
        }
        toolbar.setTitle(
            title
        );
    }

    /**
     * Clear data for creating new account
     */
    public void clearOldAccount(){
        //TODO: clear current user url
        //clear SharePreference
        SharedPrefsUtils.Companion.removeKeyValue(this, Constants.Companion.getPACKAGE_LIST());
        SharedPrefsUtils.Companion.removeKeyValue(this, Constants.Companion.getSELECTED_GROUPS());
        SharedPrefsUtils.Companion.removeKeyValue(this, Constants.Companion.getSYNC_TYPE());
    }

    //TODO: Replace this logic to another place
    /**
     * Save account in system and credentials in app.
     *
     * @param intent Intent must contains all data for saving:
     *               <ol>
     *                  <li>{@link AccountManager#KEY_ACCOUNT_NAME} for username</li>
     *                  <li>{@link #PARAM_USER_PASS} for password</li>
     *                  <li>{@link #PARAM_USER_SERVER} for server address</li>
     *               </ol>
     */
    public void finishLogin(Intent intent) {
        Log.d(TAG, "> finishLogin");

        //before add new account, clear old account data.
        clearOldAccount();

        //get values
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        String accountServer = intent.getStringExtra(PARAM_USER_SERVER);

        // Creating the account on the device and setting the auth token we got
        // (Not setting the auth token will cause another call to the server to authenticate the user)
        Log.d(TAG, "finishLogin > addAccountExplicitly" + " " + intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        final Account account = new Account(accountName, Constants.Companion.getACCOUNT_TYPE());
        mAccountManager.addAccountExplicitly(account, accountPassword, null);
        mAccountManager.setUserData(account, AccountManager.KEY_USERDATA, accountName);
        mAccountManager.setUserData(account, AccountManager.KEY_PASSWORD, accountPassword);
        mAccountManager.setUserData(account, AuthenticatorActivity.PARAM_USER_SERVER, accountServer);

        //grant permission if adding user from the third-party app (UID,PackageName);
        String packaName = getIntent().getStringExtra(PARAM_APP_PACKAGENAME);
        int uid = getIntent().getIntExtra(PARAM_APP_UID, 0);
        Log.d(TAG, packaName + ", " + getPackageName());
        //only if adding account from the third-party apps exclude android.uid.system, this will execute to grant permission and set token
        if (packaName != null && !packaName.contains("android.uid.system")) {
            AppContext.Companion.addAuthorizedApp(packaName);
            String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            if (!TextUtils.isEmpty(authToken)) {
                String authTokenType = getIntent().getStringExtra(KEY_AUTH_TOKEN_TYPE);
                mAccountManager.setAuthToken(account, authTokenType, authToken);
            }
        }

        //return value to AccountManager
        Intent intentReturn = new Intent();
        intentReturn.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.Companion.getACCOUNT_TYPE());
        intentReturn.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
        setAccountAuthenticatorResult(intentReturn.getExtras());
        setResult(RESULT_OK, intentReturn);
        Log.d(TAG, ">" + "finish return");
        finish();
        startActivity(
            new Intent(this, SyncSettingsActivity.class)
        );
    }

    /**
     * Must show progress and call {@link Subscription#unsubscribe()} on subscription object
     * in case of cancelling dialog.
     *
     * @param message Message to show to user
     * @param subscription Subscription to
     *
     * @since 0.4.2
     */
    public void showProgress(CharSequence message, final Subscription subscription) {
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
                subscription.unsubscribe();
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

    /**
     * Hide keyboard or other input method.
     */
    public void hideInputMethod(){
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
