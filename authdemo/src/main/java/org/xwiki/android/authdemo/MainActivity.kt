package org.xwiki.android.authdemo

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.xwiki.android.authdemo.databinding.ActivityMainBinding
import java.io.IOException

private val STATE_DIALOG = "state_dialog"
private val STATE_INVALIDATE = "state_invalidate"

/**
 * Open market with application page.
 *
 * @param context Context to know where from to open market
 */
private fun openAppMarket(context: Context) {
    val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "org.xwiki.android.sync"))
    var marketFound = false
    // find all applications able to handle our rateIntent
    val otherApps = context.packageManager.queryIntentActivities(rateIntent, 0)
    for (otherApp in otherApps) {
        // look for Google Play application
        if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {
            val otherAppActivity = otherApp.activityInfo
            val componentName = ComponentName(
                otherAppActivity.applicationInfo.packageName,
                otherAppActivity.name
            )
            rateIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            rateIntent.component = componentName
            context.startActivity(rateIntent)
            marketFound = true
            break
        }
    }
    // if GooglePlay not present on device, open web browser
    if (!marketFound) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
        )
        context.startActivity(webIntent)
    }
}

/**
 * MainActivity
 */
class MainActivity: AppCompatActivity() {

    private val TAG = this.javaClass.simpleName
    private lateinit var mAccountManager: AccountManager
    private var mAlertDialog: AlertDialog? = null
    private var mInvalidate: Boolean = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mAccountManager = AccountManager.get(this)

        binding.signatureTextview.text =
            "Signature=" + SystemTools.getSign(this, packageName) + "\nPackageName=" + packageName

        binding.btnAddAccount.setOnClickListener {
            addNewAccount(
                Constants.ACCOUNT_TYPE,
                Constants.AUTHTOKEN_TYPE_FULL_ACCESS
            )
        }

        binding.btnGetAuthToken.setOnClickListener {
            showAccountPicker(
                Constants.AUTHTOKEN_TYPE_FULL_ACCESS,
                false
            )
        }

        binding.btnGetAuthTokenConvenient.setOnClickListener {
            getTokenForAccountCreateIfNeeded(
                Constants.ACCOUNT_TYPE,
                Constants.AUTHTOKEN_TYPE_FULL_ACCESS
            )
        }

        binding.btnInvalidateAuthToken.setOnClickListener {
            showAccountPicker(
                Constants.AUTHTOKEN_TYPE_FULL_ACCESS,
                true
            )
        }

        binding.btnConfirmCredentials.setOnClickListener {
            val account = Account("fitz", Constants.ACCOUNT_TYPE)
            confirmCredentials(account)
        }

        if (savedInstanceState != null) {
            val showDialog = savedInstanceState.getBoolean(STATE_DIALOG)
            val invalidate = savedInstanceState.getBoolean(STATE_INVALIDATE)
            if (showDialog) {
                showAccountPicker(Constants.AUTHTOKEN_TYPE_FULL_ACCESS, invalidate)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mAlertDialog != null && mAlertDialog!!.isShowing) {
            outState.putBoolean(STATE_DIALOG, true)
            outState.putBoolean(STATE_INVALIDATE, mInvalidate)
        }
    }

    /**
     * Add new account to the account manager
     * @param accountType
     * @param authTokenType
     */
    private fun addNewAccount(accountType: String, authTokenType: String) {
        val future = mAccountManager.addAccount(accountType, authTokenType, null, null, this, { future ->
            try {
                val bnd = future.result
                showMessage("Account was created")
                Log.d(TAG, "AddNewAccount Bundle is $bnd")

            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message == "bind failure") {
                    showAlertBox()
                }
            }
        }, null)
    }

    private fun showAlertBox() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_not_found))
        builder.setMessage(getString(R.string.click_continue_to_install_app))
        builder.setCancelable(true)
        builder.setPositiveButton(getString(R.string.positive_button_text)) { dialog, id -> openAppMarket(this@MainActivity) }
        builder.setNegativeButton(getString(R.string.negative_button_text)) { dialog, id -> dialog.cancel() }

        val alert = builder.create()
        alert.show()
    }

    /**
     * Show all the accounts registered on the account manager. Request an auth token upon user select.
     * @param authTokenType
     */
    private fun showAccountPicker(authTokenType: String, invalidate: Boolean) {
        mInvalidate = invalidate
        val availableAccounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)

        if (availableAccounts.size == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show()
        } else {
            val name = arrayOfNulls<String>(availableAccounts.size)
            for (i in availableAccounts.indices) {
                name[i] = availableAccounts[i].name
            }

            // Account picker
            mAlertDialog = AlertDialog.Builder(this).setTitle("Pick Account").setAdapter(
                ArrayAdapter<String>(
                    baseContext,
                    android.R.layout.simple_list_item_1,
                    name
                )
            ) { dialog, which ->
                if (invalidate)
                    invalidateAuthToken(availableAccounts[which], authTokenType)
                else
                    getExistingAccountAuthToken(availableAccounts[which], authTokenType)
            }.create()
            mAlertDialog!!.show()
        }
    }


    private fun confirmCredentials(account: Account) {
        val future = mAccountManager.confirmCredentials(account, null, this, { future ->
            try {
                val bnd = future.result
                //showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
                Log.d(TAG, "GetToken Bundle is $bnd")
            } catch (e: OperationCanceledException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: AuthenticatorException) {
                e.printStackTrace()
            }
        }, null)
    }

    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private fun getExistingAccountAuthToken(account: Account, authTokenType: String) {
        val future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null)

        Thread(Runnable {
            try {
                val bnd = future.result
                isValidToken(bnd)
                Log.d(TAG, "GetToken Bundle is $bnd")
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage(e.message)
            }
        }).start()
    }

    /**
     * Invalidates the auth token for the account
     * @param account
     * @param authTokenType
     */
    private fun invalidateAuthToken(account: Account, authTokenType: String) {
        val future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null)

        Thread(Runnable {
            try {
                val bnd = future.result
                val authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
                val server = bnd.getString(Constants.SERVER_ADDRESS)
                mAccountManager.invalidateAuthToken(account.type, authToken)
                showMessage(account.name + " invalidated")
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage(e.message)
            }
        }).start()
    }

    /**
     * Get an auth token for the account.
     * If not exist - add it and then return its auth token.
     * If one exist - return its auth token.
     * If more than one exists - show a picker and return the select account's auth token.
     * @param accountType
     * @param authTokenType
     */
    private fun getTokenForAccountCreateIfNeeded(accountType: String, authTokenType: String) {
        val future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
            { future ->
                var bnd: Bundle? = null
                try {
                    bnd = future.result
                    isValidToken(bnd)
                    Log.d(TAG, "GetTokenForAccount Bundle is $bnd")
                } catch (e: Exception) {
                    e.printStackTrace()
                    showMessage(e.message)
                }
            }, null
        )
    }

    @Throws(IOException::class)
    private fun isValidToken(bnd: Bundle) {
        val authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
        val url = bnd.getString(Constants.SERVER_ADDRESS)
        if (authToken == null || url == null) {
            val errorMessage = bnd.getString(AccountManager.KEY_ERROR_MESSAGE)
            showMessage("FAIL! getAuthToken error message=" + errorMessage!!)
            return
        }
        XWikiHttp.isValidToken(url, authToken, object: Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    showMessage("valid token! token=$authToken")
                } else {
                    showMessage("invalid token!!! statusCode=" + response.code() + ", token=" + authToken)
                }
                response.close()
            }
        })
    }

    private fun showMessage(msg: String?) {
        if (TextUtils.isEmpty(msg))
            return

        runOnUiThread { Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show() }
    }
}
