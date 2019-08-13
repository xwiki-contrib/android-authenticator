package org.xwiki.android.sync.activities.OIDC

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import org.xwiki.android.sync.*
import org.xwiki.android.sync.activities.OIDC.OIDCActivity.OIDCActivity.selectedAc
import org.xwiki.android.sync.auth.AuthenticatorActivity
import org.xwiki.android.sync.databinding.ActOidcChooseAccountBinding
import org.xwiki.android.sync.utils.AccountClickListener

class OIDCActivity: AccountAuthenticatorActivity(), AccountClickListener {

    private lateinit var binding: ActOidcChooseAccountBinding

    object OIDCActivity{
        var selectedAc = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.act_oidc_choose_account)

        init()

        binding.lvAddAnotherAccount.setOnClickListener {
            addNewAccount()
        }
    }

    private fun init() {
        val mAccountManager = AccountManager.get(applicationContext)
        val availableAccountsList = mAccountManager.getAccountsByType(ACCOUNT_TYPE)

        if (availableAccountsList.isEmpty()) {
            addNewAccount()
        }

        val adapter = OIDCAccountAdapter(this, availableAccountsList, this)
        binding.lvSelectAccount.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            // something went wrong
            return
        }
        when (requestCode) {
            REQUEST_ACCESS_TOKEN -> {
                if (data != null) {
                    println(data)
//                if (accessToken.isNullOrEmpty()) {
//                    Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
//                } else {
//                    val i = Intent()
//                    i.putExtra(AccountManager.KEY_AUTHTOKEN, accessToken)
//                    setResult(Activity.RESULT_OK, i)
//                    finish()
//                }
                }
            }
            REQUEST_NEW_ACCOUNT -> {
                val mAccountManager = AccountManager.get(applicationContext)
                val availableAccountsList = mAccountManager.getAccountsByType(ACCOUNT_TYPE)

                val adapter = OIDCAccountAdapter(this, availableAccountsList, this)
                binding.lvSelectAccount.adapter = adapter

                selectedAc = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME).toString()
                startActivityForResult(Intent (this, OIDCAuthenticatorActivity::class.java), REQUEST_ACCESS_TOKEN)
            }
        }
    }

    override fun invoke(selectedAcc: Account) {
        selectedAc = selectedAcc.name
        startActivityForResult(Intent (this, OIDCAuthenticatorActivity::class.java), REQUEST_ACCESS_TOKEN)
    }

    private fun addNewAccount() {
        val i = Intent(this, AuthenticatorActivity::class.java)
        i.putExtra(ADD_NEW_ACCOUNT, true)
        startActivityForResult(i, REQUEST_NEW_ACCOUNT)
    }
}