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
package org.xwiki.android.sync.auth

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.AppContext.Companion.currentBaseUrl
import org.xwiki.android.sync.Constants
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.BaseViewFlipper
import org.xwiki.android.sync.activities.SettingServerIpViewFlipper
import org.xwiki.android.sync.activities.SignInViewFlipper
import org.xwiki.android.sync.activities.SyncSettingsActivity
import org.xwiki.android.sync.utils.IntentUtils
import org.xwiki.android.sync.utils.PermissionsUtils
import org.xwiki.android.sync.utils.SharedPrefsUtils
import rx.Subscription
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList

/**
 * Most important activity in authorisation process
 *
 * @version $Id: a16cb1867e9a1bfdae941273c24a77b95df1456a $
 */
class AuthenticatorActivity : AccountAuthenticatorActivity() {

    /**
     * List of flippers or nulls.
     *
     *
     * Danger, can contains nulls, not recommended to use it directly
     *
     *
     * @see .showViewFlipper
     */
    private val flippers = ArrayList<BaseViewFlipper?>()

    /**
     * Will be used for managing of user account.
     */
    private var mAccountManager: AccountManager? = null

    /**
     * Flippers root.
     */
    private var mViewFlipper: ViewFlipper? = null

    /**
     * Toolbar of current activity.
     */
    private var toolbar: Toolbar? = null

    /**
     * Current progress dialog.
     */
    private var mProgressDialog: Dialog? = null

    /**
     *
     *  1. Init view
     *  1. Init [.toolbar]
     *  1. Init [.mViewFlipper]
     *  1. Init action (settings or full auth)
     *
     *
     * @param savedInstanceState Used by default
     * @see Activity.onCreate
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_authenticator)

        val permissionsUtils = PermissionsUtils(this)
        if (!permissionsUtils.checkPermissions()) {
            permissionsUtils.requestPermissions(REQUEST_PERMISSIONS_CODE)
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setTitle(R.string.xwikiAccount)

        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            builder =
                AlertDialog.Builder(this@AuthenticatorActivity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this@AuthenticatorActivity)
        }
        builder.setTitle(R.string.xwiki)
            .setIcon(resources.getDrawable(R.drawable.logo))
            .setMessage(R.string.signUpOfferMessage)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, which -> dialog.dismiss() }
        mViewFlipper = findViewById(R.id.view_flipper)
        val position: Int?
        mAccountManager = AccountManager.get(applicationContext)
        val availableAccounts = mAccountManager!!.getAccountsByType(Constants.ACCOUNT_TYPE)
        position = 0
        if (availableAccounts.size > 0) {
            Toast.makeText(this, "The user already exists!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        showViewFlipper(position)
    }

    /**
     * Intercept back button pressing and remap it to flipper if current flipper child is
     * not settings.
     */
    override fun onBackPressed() {
        if (mViewFlipper!!.displayedChild == orderOfFlippers.indexOf(SettingServerIpViewFlipper::class.java)) {
            super.onBackPressed()
        } else {
            doPrevious(
                mViewFlipper!!.currentView
            )
        }
    }

    /**
     * Must be called when current flipper must receive calling of
     * [BaseViewFlipper.doPrevious] and be changed to previous.
     *
     * @param view View, which trigger action
     */
    fun doPrevious(view: View) {
        val position = mViewFlipper!!.displayedChild
        chooseAnimation(position == orderOfFlippers.indexOf(SettingServerIpViewFlipper::class.java))
        flippers[position]!!.doPrevious()
        showViewFlipper(position - 1)
    }

    /**
     * Must be called when current flipper must receive calling of
     * [BaseViewFlipper.doNext] and be changed to next.
     *
     * @param view View, which trigger action
     */
    fun doNext(view: View) {
        val position = mViewFlipper!!.displayedChild
        chooseAnimation(true)
        flippers[position]!!.doNext()
        if (position + 1 >= orderOfFlippers.size) {
            finish()
        }
        showViewFlipper(position + 1)
    }

    /**
     * Util method, choose animation in dependency to toNext.
     *
     * @param toNext If true - will be used animation right-to-left (<-),
     * left-to-right otherwise (->)
     *
     * @since 0.4.2
     */
    private fun chooseAnimation(toNext: Boolean) {
        if (toNext) {
            mViewFlipper!!.inAnimation = AnimationUtils.loadAnimation(
                this,
                R.anim.push_left_in
            )
            mViewFlipper!!.outAnimation = AnimationUtils.loadAnimation(
                this,
                R.anim.push_left_out
            )
        } else {
            mViewFlipper!!.inAnimation = AnimationUtils.loadAnimation(
                this,
                R.anim.push_right_in
            )
            mViewFlipper!!.outAnimation = AnimationUtils.loadAnimation(
                this,
                R.anim.push_right_out
            )
        }
    }

    //TODO:: Replace by normal registration
    /**
     * Will be called when user push to "Create one" button.
     *
     * @param view View which trigger action
     */
    fun signUp(view: View) {
        var url = currentBaseUrl()
        if (url.endsWith("/")) {
            url += "bin/view/XWiki/Registration"
        } else {
            url += "/bin/view/XWiki/Registration"
        }
        val intent = IntentUtils.openLink(
            url
        )
        startActivity(intent)
    }

    /**
     * Set current visible element and init flipper if it needed.
     *
     *
     *
     * Procedure of initialisation:
     *
     *
     *
     *  1.  Get class of flipper
     *  1.  Get constructor which signature duplicate
     * [BaseViewFlipper.BaseViewFlipper]
     *
     *  1.  Create instance using "this" and [.mViewFlipper]
     *
     *
     * @param position Position of item which must be shown
     *
     * @since 0.4.2
     */
    fun showViewFlipper(position: Int) {
        mViewFlipper!!.displayedChild = position
        while (flippers.size <= position) {
            flippers.add(null)
        }
        var flipper: BaseViewFlipper? = flippers[position]
        if (flipper == null) {
            try {
                flipper = orderOfFlippers[position].getConstructor(
                    AuthenticatorActivity::class.java,
                    View::class.java
                ).newInstance(
                    this,
                    mViewFlipper!!.getChildAt(position)
                )
                flippers[position] = flipper
            } catch (e: InstantiationException) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e)
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e)
            } catch (e: InvocationTargetException) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e)
            } catch (e: NoSuchMethodException) {
                Log.e(TAG, "View flipper must contains constructor with activity and view", e)
            } finally {
                if (flipper == null) {
                    return
                }
            }
        }
        var title = flipper.title
        if (title == null) {
            title = getString(R.string.app_name)
        }
        toolbar!!.title = title
    }

    /**
     * Clear data for creating new account
     */
    fun clearOldAccount() {
        //TODO: clear current user url
        //clear SharePreference
        SharedPrefsUtils.removeKeyValue(this, Constants.PACKAGE_LIST)
        SharedPrefsUtils.removeKeyValue(this, Constants.SELECTED_GROUPS)
        SharedPrefsUtils.removeKeyValue(this, Constants.SYNC_TYPE)
    }

    //TODO: Replace this logic to another place
    /**
     * Save account in system and credentials in app.
     *
     * @param intent Intent must contains all data for saving:
     *
     *  1. [AccountManager.KEY_ACCOUNT_NAME] for username
     *  1. [.PARAM_USER_PASS] for password
     *  1. [.PARAM_USER_SERVER] for server address
     *
     */
    fun finishLogin(intent: Intent) {
        Log.d(TAG, "> finishLogin")

        //before add new account, clear old account data.
        clearOldAccount()

        //get values
        val accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val accountPassword = intent.getStringExtra(PARAM_USER_PASS)
        val accountServer = intent.getStringExtra(PARAM_USER_SERVER)

        // Creating the account on the device and setting the auth token we got
        // (Not setting the auth token will cause another call to the server to authenticate the user)
        Log.d(TAG, "finishLogin > addAccountExplicitly" + " " + intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
        val account = Account(accountName, Constants.ACCOUNT_TYPE)
        mAccountManager!!.addAccountExplicitly(account, accountPassword, null)
        mAccountManager!!.setUserData(account, AccountManager.KEY_USERDATA, accountName)
        mAccountManager!!.setUserData(account, AccountManager.KEY_PASSWORD, accountPassword)
        mAccountManager!!.setUserData(account, AuthenticatorActivity.PARAM_USER_SERVER, accountServer)

        //grant permission if adding user from the third-party app (UID,PackageName);
        val packaName = getIntent().getStringExtra(PARAM_APP_PACKAGENAME)
        val uid = getIntent().getIntExtra(PARAM_APP_UID, 0)
        Log.d(TAG, "$packaName, $packageName")
        //only if adding account from the third-party apps exclude android.uid.system, this will execute to grant permission and set token
        if (packaName != null && !packaName.contains("android.uid.system")) {
            AppContext.addAuthorizedApp(packaName)
            val authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN)
            if (!TextUtils.isEmpty(authToken)) {
                val authTokenType = getIntent().getStringExtra(KEY_AUTH_TOKEN_TYPE)
                mAccountManager!!.setAuthToken(account, authTokenType, authToken)
            }
        }

        //return value to AccountManager
        val intentReturn = Intent()
        intentReturn.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
        intentReturn.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
        setAccountAuthenticatorResult(intentReturn.extras)
        setResult(Activity.RESULT_OK, intentReturn)
        Log.d(TAG, ">" + "finish return")
        finish()
        startActivity(
            Intent(this, SyncSettingsActivity::class.java)
        )
    }

    /**
     * Must show progress and call [Subscription.unsubscribe] on subscription object
     * in case of cancelling dialog.
     *
     * @param message Message to show to user
     * @param subscription Subscription to
     *
     * @since 0.4.2
     */
    fun showProgress(message: CharSequence, subscription: Subscription) {
        // To avoid repeatedly create
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            return
        }
        val dialog = ProgressDialog(this)
        dialog.setMessage(message)
        dialog.isIndeterminate = true
        dialog.setCancelable(true)
        dialog.setOnCancelListener {
            Log.i(TAG, "user cancelling authentication")
            subscription.unsubscribe()
        }
        // We save off the progress dialog in a field so that we can dismiss
        // it later.
        mProgressDialog = dialog
        mProgressDialog!!.show()
    }

    /**
     * Hides the progress UI for a lengthy operation.
     */
    fun hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
            mProgressDialog = null
        }
    }

    /**
     * Hide keyboard or other input method.
     */
    fun hideInputMethod() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        /**
         * Contains order of flippers in authorisation progress.
         *
         *
         *
         * All flippers must support constructor
         * [BaseViewFlipper.BaseViewFlipper], because
         * all instances will be created automatically using reflection in
         * [.showViewFlipper]
         *
         */
        private val orderOfFlippers: MutableList<Class<out BaseViewFlipper>>

        init {
            orderOfFlippers = ArrayList()
            orderOfFlippers.add(SettingServerIpViewFlipper::class.java)
            orderOfFlippers.add(SignInViewFlipper::class.java)
        }

        /**
         * Tag which will be used for logging
         */
        private val TAG = "AuthenticatorActivity"

        val KEY_AUTH_TOKEN_TYPE = "KEY_AUTH_TOKEN_TYPE"
        val PARAM_USER_SERVER = "XWIKI_USER_SERVER"
        val PARAM_USER_PASS = "XWIKI_USER_PASS"
        val PARAM_APP_UID = "PARAM_APP_UID"
        val PARAM_APP_PACKAGENAME = "PARAM_APP_PACKAGENAME"
        val IS_SETTING_SYNC_TYPE = "IS_SETTING_SYNC_TYPE"

        /**
         * Code which await to returns for requesting permissions
         */
        private val REQUEST_PERMISSIONS_CODE = 1
    }
}
