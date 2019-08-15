package org.xwiki.android.sync.activities.OIDC

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import org.xwiki.android.sync.databinding.ActOidcChooseAccountBinding
import org.xwiki.android.sync.utils.AccountClickListener
import org.xwiki.android.sync.utils.extensions.TAG

private suspend fun createAuthorizationCodeFlow(selectedAccountName: String): AuthorizationCodeFlow {
    val userServerBaseUrl = userAccountsRepo.findByAccountName(
        selectedAccountName
    ) ?.serverAddress ?: throw IllegalArgumentException(
        "Selected account name is absent in databse"
    )

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

class OIDCActivity: AppCompatActivity(), AccountClickListener {

    private var selectedAccountName: String? = null

    private lateinit var binding: ActOidcChooseAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.act_oidc_choose_account)

        init()
    }

    private fun init() {
        val mAccountManager = AccountManager.get(applicationContext)
        val availableAccountsList = mAccountManager.getAccountsByType(ACCOUNT_TYPE)

        if (availableAccountsList.isEmpty()) {
            addNewAccount()
        }

        val adapter = OIDCAccountAdapter(this, availableAccountsList, this)

        binding.lvAddAnotherAccount.setOnClickListener {
            addNewAccount()
        }

        binding.lvSelectAccount.adapter = adapter
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
            } catch (ex: Exception) {
                Log.e(TAG, ex.message)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_NEW_ACCOUNT -> {
                val mAccountManager = AccountManager.get(applicationContext)
                val availableAccountsList = mAccountManager.getAccountsByType(ACCOUNT_TYPE)

                val adapter = OIDCAccountAdapter(this, availableAccountsList, this)
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
            requestAccessToken(createAuthorizationCodeFlow(accountName), authorizationCode)
        }
    }

    override fun invoke(selectedAccount: Account) {
        appCoroutineScope.launch {
            startAuthorization(selectedAccount.name)
        }
    }

    private suspend fun startAuthorization(accountName: String) {
        try {
            selectedAccountName = accountName
            val flow: AuthorizationCodeFlow = createAuthorizationCodeFlow(accountName)
            flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build().let {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(browserIntent)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Failed")
        }
    }

    private fun addNewAccount() {
        val i = Intent(this, AuthenticatorActivity::class.java)
        i.putExtra(ADD_NEW_ACCOUNT, true)
        startActivityForResult(i, REQUEST_NEW_ACCOUNT)
    }
}