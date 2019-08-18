package org.xwiki.android.sync.activities.OIDC

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.ClientParametersAuthentication
import com.google.api.client.auth.openidconnect.IdTokenResponse
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xwiki.android.sync.*
import org.xwiki.android.sync.auth.AuthenticatorActivity
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.databinding.ActOidcChooseAccountBinding
import org.xwiki.android.sync.utils.AccountClickListener
import org.xwiki.android.sync.utils.extensions.TAG
import java.io.IOException
import android.webkit.CookieSyncManager
import org.xwiki.android.sync.utils.OIDCWebViewClient
import org.xwiki.android.sync.utils.WebViewPageLoadedListener

private suspend fun createAuthorizationCodeFlow(selectedAccountName: String, serverUrl: String): AuthorizationCodeFlow {
    val userServerBaseUrl: String = if (serverUrl.isNullOrEmpty()) {
        userAccountsRepo.findByAccountName(
            selectedAccountName
        ) ?.serverAddress ?: throw IllegalArgumentException(
            "Selected account name is absent in databse"
        )
    } else {
        serverUrl
    }

    return AuthorizationCodeFlow.Builder(
        BearerToken.authorizationHeaderAccessMethod(),
        NetHttpTransport(),
        JacksonFactory(),
        GenericUrl(
            buildOIDCTokenServerUrl(
                userServerBaseUrl
            )
        ),
        ClientParametersAuthentication(
            selectedAccountName,
            ""
        ),
        selectedAccountName,
        buildOIDCAuthorizationServerUrl(
            userServerBaseUrl
        )
    ).apply {
        scopes = mutableListOf(
            "openid",
            "offline_access",
            "profile"
        )
    }.build()
}

class OIDCActivity: AppCompatActivity(), AccountClickListener, WebViewPageLoadedListener {

    private var selectedAccountName: String? = null

    private lateinit var binding: ActOidcChooseAccountBinding

    private var allUsersList: List<UserAccount> = listOf()

    private var clientID = ""

    private var serverUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.act_oidc_choose_account)

        if (!intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME).isNullOrEmpty()) {
            clientID = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        }

        if (!intent.getStringExtra("serverUrl").isNullOrEmpty()) {
            serverUrl = intent.getStringExtra("serverUrl")
        }
        if (!clientID.isEmpty()) {
            binding.cvOIDCAccountList.visibility = View.GONE
            binding.tvPleaseWait.visibility = View.VISIBLE
            binding.webview.visibility  = View.VISIBLE
        }

        init()
    }

    private fun init() {
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush()
            cookieManager.removeAllCookies(null)
            cookieManager.removeSessionCookies(null)
        } else {
            val cookieSyncManager = CookieSyncManager.createInstance(this)
            cookieSyncManager.startSync()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncManager.stopSync()
        }

        appCoroutineScope.launch {
            allUsersList = userAccountsRepo.getAll()

            if (!clientID.isEmpty()) {
                startAuthorization(clientID)
            } else {
                if (allUsersList.isEmpty()) {
                    addNewAccount()
                }
                val adapter = OIDCAccountAdapter(
                    this@OIDCActivity,
                    allUsersList,
                    this@OIDCActivity
                )

                binding.lvAddAnotherAccount.setOnClickListener {
                    addNewAccount()
                }

                binding.lvSelectAccount.adapter = adapter
            }
        }
    }

    private fun extractAuthorizationCode(intent: Intent): String? {
        val data = intent.dataString
        val uri = Uri.parse(data)

        return uri.getQueryParameter("code")
    }

    private fun requestAccessToken(flow: AuthorizationCodeFlow, authorizationCode: String?) {
        appCoroutineScope.launch {
            try {
                val idTokenResponse = IdTokenResponse.execute(
                    flow.newTokenRequest(
                        authorizationCode
                    ).setRedirectUri(
                        REDIRECT_URI
                    )
                )
                Log.d(TAG, idTokenResponse.parseIdToken().payload["sub"].toString())
                val accessToken = idTokenResponse.accessToken

                withContext(Dispatchers.Main) { // launch on UI thread. TODO:: Check necessarily of UI scope
                    sendResult(accessToken)
                }
            }  catch(ex: IOException) {
                Log.e(TAG, ex.message)
                withContext(Dispatchers.Main) {
                    sendResult("")
                    finish()
                }
            }
            catch (ex: Exception) {
                Log.e(TAG, ex.message)
                withContext(Dispatchers.Main) {
                    sendResult("")
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_NEW_ACCOUNT -> {
                appCoroutineScope.launch {
                    allUsersList = userAccountsRepo.getAll()
                }

                val adapter = OIDCAccountAdapter(this, allUsersList, this)
                binding.lvSelectAccount.adapter = adapter

                val accountName = data ?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) ?.toString() ?: return
                appCoroutineScope.launch {
                    startAuthorization(accountName)
                }
            }
        }
    }

    private fun sendResult(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent()
            i.putExtra(AccountManager.KEY_ACCOUNT_NAME, clientID)
            i.putExtra(AccountManager.KEY_AUTHTOKEN, accessToken)
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        val accountName = selectedAccountName ?: return
        val authorizationCode = extractAuthorizationCode(intent)
        appCoroutineScope.launch {
            requestAccessToken(createAuthorizationCodeFlow(accountName, serverUrl), authorizationCode)
        }
    }

    override fun invoke(selectedAccount: UserAccount) {
        binding.webview.visibility  = View.VISIBLE
        appCoroutineScope.launch {
            startAuthorization(selectedAccount.accountName)
        }
    }

    private suspend fun startAuthorization(accountName: String) {
        try {
            selectedAccountName = accountName
            val flow: AuthorizationCodeFlow = createAuthorizationCodeFlow(accountName, serverUrl)
            flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build().let {
                appCoroutineScope.launch (Dispatchers.Main) {
                    binding.webview.webViewClient = OIDCWebViewClient(this@OIDCActivity, accountName)
                    binding.webview.loadUrl(it)
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }
    }

    private fun addNewAccount() {
        val i = Intent(this, AuthenticatorActivity::class.java)
        i.putExtra(ADD_NEW_ACCOUNT, true)
        startActivityForResult(i, REQUEST_NEW_ACCOUNT)
    }

    override fun onPageLoaded(authorizationCode: String?, accountName: String) {
        appCoroutineScope.launch {
            requestAccessToken(createAuthorizationCodeFlow(accountName, serverUrl), authorizationCode)
        }
    }
}