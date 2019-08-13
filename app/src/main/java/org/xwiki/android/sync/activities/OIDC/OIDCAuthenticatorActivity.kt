package org.xwiki.android.sync.activities.OIDC

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import org.xwiki.android.sync.activities.OIDC.OIDCActivity.OIDCActivity.selectedAc
import org.xwiki.android.sync.utils.extensions.TAG
import java.io.IOException

private fun createAuthorizationCodeFlow(): AuthorizationCodeFlow {
    return AuthorizationCodeFlow.Builder(
        BearerToken.authorizationHeaderAccessMethod(),
        NetHttpTransport(),
        JacksonFactory(),
        GenericUrl(TOKEN_SERVER_URL),
        ClientParametersAuthentication(
            selectedAc,
            ""
        ),
        selectedAc,
        AUTHORIZATION_SERVER_URL
    ).apply {
        scopes = mutableListOf(
            "openid",
            "offline_access",
            "profile"
        )
    }.build()
}

class OIDCAuthenticatorActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val flow: AuthorizationCodeFlow = createAuthorizationCodeFlow()

            if (!isRedirect(intent)) {
                flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build().let {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    startActivity(browserIntent)
                }
            }
//            else {
//                val authorizationCode = extractAuthorizationCode(intent)
//                flow.let { GetTokens(it).execute(authorizationCode) }
//            }
        } catch (ex: Exception) {
            Log.e(TAG, "Failed")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent ?: return // check for null

        try {
            val flow = createAuthorizationCodeFlow()

            if (isRedirect(intent)) {
                val authorizationCode = extractAuthorizationCode(intent)

                requestAccessToken(flow, authorizationCode)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Failed")
        }
    }

    private fun isRedirect(intent: Intent): Boolean {
        val data = intent.dataString
        return Intent.ACTION_VIEW == intent.action && data != null
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

    private fun sendResult(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            // Here I'm sending the access token but the the activity is not receiving.
            //That's why I wish to call public method of OIDCActivity to send the token.
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, accessToken)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}