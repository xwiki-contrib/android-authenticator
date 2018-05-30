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
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;



/**
 * A grant permission activity.
 * input your count's password and verify by comparing with local account's info
 * or sending password to server to verify according the response.
 */
public class GrantPermissionActivity extends AccountAuthenticatorActivity {

    // UI references.
    private String authTokenType = null;
    private String accountName = null;
    private String accountType = null;

    //get third-party app's informations from getIntent.
    private int uid = 0;
    private String packageName = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_grant_permission);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("XWiki Account");
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));

        //get data from intent
        uid = getIntent().getIntExtra("uid", 0);
        packageName = getIntent().getStringExtra("packageName");
        accountName = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        authTokenType = getIntent().getStringExtra(AuthenticatorActivity.KEY_AUTH_TOKEN_TYPE);
        //check null, if null return.
        if (uid == 0 || accountName == null) {
            Toast.makeText(this, "null uid or accountName", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //init view
        ((TextView) findViewById(R.id.packageName)).setText(packageName);
        ((TextView) findViewById(R.id.accountName)).setText(accountName);
    }



    public void onCancel(View view){
        finish();
    }

    public void onHandleAuthorize(View view){
        AppContext.addAuthorizedApp(uid, packageName);
        AccountManager mAccountManager = AccountManager.get(getApplicationContext());
        Account account = new Account(accountName, Constants.ACCOUNT_TYPE);
        String authToken = SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, null);
        mAccountManager.setAuthToken(account, authTokenType, authToken);
        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
        intent.putExtra(Constants.SERVER_ADDRESS, XWikiHttp.getServerAddress());
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
}

