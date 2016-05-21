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
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.rest.HttpResponse;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;

import java.io.IOException;


/**
 * A grant permission activity.
 * input your count's password and verify by comparing with local account's info
 * or sending password to server to verify according the response.
 */
public class GrantPermissionActivity extends AppCompatActivity {

    // UI references.
    private EditText mPasswdEditText;
    private String accountName = null;
    private String accountPasswd = null;

    //get third-party app's informations from getIntent.
    private int uid = 0;
    private String packageName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_grant_permission);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //left back arrow and material design.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));

        mPasswdEditText = (EditText) findViewById(R.id.accountPassword);

        //get data from intent
        uid = getIntent().getIntExtra("uid", 0);
        packageName = getIntent().getStringExtra("packageName");
        accountName = getIntent().getStringExtra("accountName");
        //check null, if null return.
        if (uid == 0 || accountName == null) {
            Toast.makeText(this, "null uid or accountName", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //init view
        String packageName = getPackageManager().getNameForUid(uid);
        ((TextView) findViewById(R.id.packageName)).setText(packageName);
        ((TextView) findViewById(R.id.accountName)).setText(accountName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleAuthorize(View view) {
        accountPasswd = mPasswdEditText.getText().toString();

        if (TextUtils.isEmpty(accountPasswd)) {
            Toast.makeText(this, "Please input your password!", Toast.LENGTH_SHORT).show();
            return;
        }

        //just compare the local passwd with user's input to grant permission
        //TODO maybe have some security issue.
        AccountManager mAccountManager = AccountManager.get(getApplicationContext());
        Account account = new Account(accountName, Constants.ACCOUNT_TYPE);
        String password = mAccountManager.getUserData(account, AccountManager.KEY_PASSWORD);
        if (accountPasswd.equals(password)) {
            AppContext.addAuthorizedApp(uid, packageName);
            finish();
        } else {
            Toast.makeText(GrantPermissionActivity.this, "Password error! Try again please!", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    /**
     * login from server to verify the user's passwd in order to grant permission.
     */
    private void loginFromServer() {
        new AsyncTask<String, String, Intent>() {
            @Override
            protected Intent doInBackground(String... params) {
                //get url from server requestUrl=202.176.99.1:8080, xwiki.org and so on.
                String url = SharedPrefsUtils.getValue(getApplicationContext(), Constants.SERVER_ADDRESS, null);
                Intent intent = new Intent();
                HttpResponse response = null;
                try {
                    response = XWikiHttp.login(url, accountName, accountPasswd);
                    intent.putExtra("statusCode", response.getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return intent;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                int statusCode = intent.getIntExtra("statusCode", 0);
                //0: network error, or HttpExecutor.performRequest Exception.
                if (statusCode == 0) {
                    Toast.makeText(GrantPermissionActivity.this, "Network error! Try again please!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //error reponse.
                if (statusCode < 200 || statusCode > 299) {
                    Toast.makeText(GrantPermissionActivity.this, "The password is wrong! Input again please!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //ok, grant permission and finish();
                Toast.makeText(GrantPermissionActivity.this, "Authorize Successfully!", Toast.LENGTH_SHORT).show();
                AppContext.addAuthorizedApp(uid, packageName);
                finish();
            }
        }.execute();
    }

}

