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
package org.xwiki.android.sync.activities

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.AppContext.Companion.currentBaseUrl
import org.xwiki.android.sync.Constants
import org.xwiki.android.sync.R
import org.xwiki.android.sync.auth.AuthenticatorActivity
import org.xwiki.android.sync.utils.SharedPrefsUtils

/**
 * A grant permission activity.
 * input your count's password and verify by comparing with local account's info
 * or sending password to server to verify according the response.
 *
 * @version $Id: 84beda489e82810e3a8fb6ca67a7681c7ab0240a $
 */
class GrantPermissionActivity : AccountAuthenticatorActivity() {

    // UI references.
    private var authTokenType: String? = null
    private var accountName: String? = null
    private var accountType: String? = null

    //get third-party app's informations from getIntent.
    private var uid = 0
    private var pkgName: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_grant_permission)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.xwikiAccount)

        //get data from intent
        uid = intent.getIntExtra("uid", 0)
        pkgName = intent.getStringExtra("packageName")
        accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        authTokenType = intent.getStringExtra(AuthenticatorActivity.KEY_AUTH_TOKEN_TYPE)
        //check null, if null return.
        if (uid == 0 || accountName == null) {
            Toast.makeText(this, "null uid or accountName", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        //init view
        (findViewById<View>(R.id.packageName) as TextView).text = packageName
        (findViewById<View>(R.id.accountName) as TextView).text = accountName
    }


    fun onCancel(view: View) {
        finish()
    }

    fun onHandleAuthorize(view: View) {
        AppContext.addAuthorizedApp(packageName)
        val mAccountManager = AccountManager.get(applicationContext)
        val account = Account(accountName, Constants.ACCOUNT_TYPE)
        val authToken =
            SharedPrefsUtils.getValue(AppContext.getInstance()!!.applicationContext, Constants.COOKIE, null)
        mAccountManager.setAuthToken(account, authTokenType, authToken)
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
        intent.putExtra(Constants.SERVER_ADDRESS, currentBaseUrl())
        setAccountAuthenticatorResult(intent.extras)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

