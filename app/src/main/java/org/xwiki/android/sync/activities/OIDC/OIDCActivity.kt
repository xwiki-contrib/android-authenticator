package org.xwiki.android.sync.activities.OIDC

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import okhttp3.*
import org.json.JSONObject
import org.xwiki.android.sync.utils.OIDCWebViewClient
import org.xwiki.android.sync.utils.WebViewPageLoadedListener
import java.net.URL

private suspend fun createAuthorizationCodeFlow(serverUrl: String): AuthorizationCodeFlow {
    return AuthorizationCodeFlow.Builder(
        BearerToken.authorizationHeaderAccessMethod(),
        NetHttpTransport(),
        JacksonFactory(),
        GenericUrl(
            buildOIDCTokenServerUrl(
                serverUrl
            )
        ),
        ClientParametersAuthentication(
            Settings.Secure.ANDROID_ID,
            ""
        ),
        Settings.Secure.ANDROID_ID,
        buildOIDCAuthorizationServerUrl(
            serverUrl
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

    private lateinit var binding: ActOidcChooseAccountBinding

    private var allUsersList: List<UserAccount> = listOf()

    private var requestNewLogin = false

    private var serverUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.act_oidc_choose_account)

        if (!intent.getStringExtra("serverUrl").isNullOrEmpty()) {
            serverUrl = intent.getStringExtra("serverUrl")
            binding.cvOIDCAccountList.visibility = View.GONE
            binding.tvPleaseWait.visibility = View.VISIBLE
            binding.webview.visibility  = View.VISIBLE
            requestNewLogin = intent.getBooleanExtra("requestNewLogin",false)
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

            if (requestNewLogin) {
                startAuthorization()
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

                serverUrl = data?.getStringExtra("serverUrl").toString()

                appCoroutineScope.launch {
                    startAuthorization()
                }
            }
        }
    }

    private fun sendResult(accessToken: String) {
        if (accessToken.isNullOrEmpty()) {
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
        } else {
            getUserInfo(accessToken)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        val authorizationCode = extractAuthorizationCode(intent)
        appCoroutineScope.launch {
            requestAccessToken(createAuthorizationCodeFlow(serverUrl), authorizationCode)
        }
    }

    override fun invoke(selectedAccount: UserAccount) {
        binding.webview.visibility  = View.VISIBLE
        serverUrl = selectedAccount.serverAddress
        appCoroutineScope.launch {
            startAuthorization()
        }
    }

    private suspend fun startAuthorization() {
        try {
            val flow: AuthorizationCodeFlow = createAuthorizationCodeFlow(serverUrl)
            flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build().let {
                appCoroutineScope.launch (Dispatchers.Main) {
                    binding.webview.webViewClient = OIDCWebViewClient(this@OIDCActivity)
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

    override fun onPageLoaded(authorizationCode: String?) {
        appCoroutineScope.launch {
            requestAccessToken(createAuthorizationCodeFlow(serverUrl), authorizationCode)
        }
    }

    private fun getUserInfo(token: String) {
        val url = URL ("$serverUrl/oidc/userinfo")

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()

        client
            .newCall(request)
            .enqueue(object :Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 200) {
                    val userInfo = JSONObject(response.body()?.string())
                    val sub = userInfo.getString("sub").split(".")
                    val i = Intent()
                    i.putExtra(AccountManager.KEY_AUTHTOKEN, token)
                    i.putExtra(AccountManager.KEY_ACCOUNT_NAME, sub[sub.size-1])
                    setResult(Activity.RESULT_OK, i)
                    finish()
                } else {
                    Toast.makeText(this@OIDCActivity, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@OIDCActivity, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}