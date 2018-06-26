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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.activities.GrantPermissionActivity;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import java.util.List;

import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.functions.Action1;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static org.xwiki.android.sync.AppContext.currentBaseUrl;
import static org.xwiki.android.sync.AppContext.getApiManager;
import static org.xwiki.android.sync.Constants.AUTHTOKEN_TYPE_FULL_ACCESS;
import static org.xwiki.android.sync.Constants.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
import static org.xwiki.android.sync.Constants.AUTHTOKEN_TYPE_READ_ONLY;
import static org.xwiki.android.sync.Constants.AUTHTOKEN_TYPE_READ_ONLY_LABEL;

/**
 * Realisation of authenticator for XWiki account. Full required management of XWiki account
 *
 * @version $Id$
 */
public class XWikiAuthenticator extends AbstractAccountAuthenticator {

    /**
     * Tag fr logging.
     */
    private static final String TAG = "XWikiAuthenticator";

    /**
     * Current session context.
     */
    private final Context mContext;

    /**
     * Standard constructor.
     *
     * @param context Context which will be set to {@link #mContext}
     */
    public XWikiAuthenticator(Context context) {
        super(context);
        this.mContext = context;
    }

    /**
     * Add account into system. In fact just refill data from parameters into new bundle. All
     * details you can get from parent documentations.
     *
     * @return Bundle with filled data: account type, auth token type, sync type, etc.
     */
    @Override
    public Bundle addAccount(
        AccountAuthenticatorResponse response,
        String accountType,
        String authTokenType,
        String[] requiredFeatures,
        Bundle options
    ) {
        Log.d("xwiki", TAG + "> addAccount");

        int uid = options.getInt(AccountManager.KEY_CALLER_UID);
        String packageName = mContext.getPackageManager().getNameForUid(uid);

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        //just for passing some param
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.KEY_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        //for granting permission
        intent.putExtra(AuthenticatorActivity.PARAM_APP_UID, uid);
        intent.putExtra(AuthenticatorActivity.PARAM_APP_PACKAGENAME, packageName);
        //true: if from XWiki Account Preference, false:if from adding account.
        intent.putExtra(AuthenticatorActivity.IS_SETTING_SYNC_TYPE, false);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /**
     * Refresh auth token of current account. All parameters info you can look in parent
     * documentations.
     *
     * @return Bundle with error if auth token type not supported, getting auth token is impossible
     * (interrupted in long-time operations or received package from server contains not auth token)
     */
    @Override
    public Bundle getAuthToken(
        AccountAuthenticatorResponse response,
        Account account,
        String authTokenType,
        Bundle options
    ) {

        Log.d("xwiki", TAG + "> getAuthToken");

        final AccountManager am = AccountManager.get(mContext);
        String accountName = am.getUserData(account, AccountManager.KEY_USERDATA);
        String accountPassword = am.getUserData(account, AccountManager.KEY_PASSWORD);

        int uid = options.getInt(AccountManager.KEY_CALLER_UID);
        String packageName = mContext.getPackageManager().getNameForUid(uid);

        if (!authTokenType.equals(Constants.AUTHTOKEN_TYPE_FULL_ACCESS + packageName)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        if (!AppContext.isAuthorizedApp(packageName)) {
            final Intent intent = new Intent(mContext, GrantPermissionActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("packageName", packageName);
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            intent.putExtra(AuthenticatorActivity.KEY_AUTH_TOKEN_TYPE, authTokenType);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

            Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        //authTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;
        // Auth token array because it can be modified in parallel threads or world.
        // In this case we will need to change it, but string immutable what we can't say
        // about array items
        // TODO::fix and rewrite by using XwikiHttp#login
        final String[] authToken = {am.peekAuthToken(account, authTokenType)};
        Log.d("xwiki", TAG + "> peekAuthToken returned - " + authToken[0]);

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken[0])) {
            //if  having no cached token, request server for new token and refreshAllAuthTokenType
            //make all cached authTokenType-tokens consistent.
            try {
                authToken[0] = null;
                Log.d("xwiki", TAG + "> re-authenticating with the existing password");
                final Object sync = new Object();
                getApiManager().getXwikiServicesApi().login(
                        Credentials.basic(accountName, accountPassword)
                ).subscribe(
                        new Action1<Response<ResponseBody>>() {
                            @Override
                            public void call(Response<ResponseBody> responseBodyResponse) {
                                synchronized (sync) {
                                    authToken[0] = responseBodyResponse.headers().get("Set-Cookie");
                                    Log.d(TAG, "XWikiAuthenticator, authtoken=" + authToken[0]);
                                    sync.notifyAll();
                                }
                            }
                        }
                );
                synchronized (sync) {
                    while (authToken[0] == null) {
                        sync.wait();
                    }
                }

                //If we get an authToken - we return it
                //refresh all tokentype for all apps' package
                refreshAllAuthTokenType(am, account, authToken[0]);
            } catch (InterruptedException e) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ERROR_MESSAGE, "getAuthToken error impossible !!!");
                return result;
            }

        }

        if (TextUtils.isEmpty(authToken[0])) {
            //if we get here, error, it's impossible!
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "getAuthToken error impossible !!!");
            return result;
        } else {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken[0]);
            result.putString(Constants.SERVER_ADDRESS, currentBaseUrl());
            return result;
        }
    }

    /**
     * @param authTokenType Identifier for label
     * @return {@link Constants#AUTHTOKEN_TYPE_FULL_ACCESS_LABEL} for
     * {@link Constants#AUTHTOKEN_TYPE_FULL_ACCESS},
     * {@link Constants#AUTHTOKEN_TYPE_READ_ONLY_LABEL} for
     * {@link Constants#AUTHTOKEN_TYPE_READ_ONLY_LABEL} or "authTokenType + (Label)" otherwise
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.d(TAG, "getAuthTokenLabel," + authTokenType);
        if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    /**
     * Answer that this account have no features
     */
    @Override
    public Bundle hasFeatures(
        AccountAuthenticatorResponse response,
        Account account,
        String[] features
    ) {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    /**
     * Do nothing
     * @return null
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    /**
     * Do nothing
     * @return null
     */
    @Override
    public Bundle confirmCredentials(
        AccountAuthenticatorResponse response,
        Account account,
        Bundle options
    ) {
        return null;
    }

    /**
     * Do nothing
     * @return null
     */
    @Override
    public Bundle updateCredentials(
        AccountAuthenticatorResponse response,
        Account account,
        String authTokenType,
        Bundle options
    ) {
        return null;
    }

    /**
     * Refresh auth tokens for all packages which can be got by field
     * {@link Constants#PACKAGE_LIST}.
     */
    public static void refreshAllAuthTokenType(
        AccountManager am,
        Account account,
        String authToken
    ) {
        List<String> packageList = SharedPrefsUtils.getArrayList(
            AppContext.getInstance().getApplicationContext(),
            Constants.PACKAGE_LIST
        );
        if (packageList == null || packageList.size() == 0) return;
        for (String item : packageList) {
            String tokenType = Constants.AUTHTOKEN_TYPE_FULL_ACCESS + item;
            am.setAuthToken(account, tokenType, authToken);
        }
    }

}
