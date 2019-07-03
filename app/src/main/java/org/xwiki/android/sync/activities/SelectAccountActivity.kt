package org.xwiki.android.sync.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.xwiki.android.sync.ACCOUNT_TYPE
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.base.BaseActivity
import org.xwiki.android.sync.databinding.ActSelectAccountBinding
import org.xwiki.android.sync.utils.AccountClickListener

class SelectAccountActivity : BaseActivity(), AccountClickListener {

    lateinit var binding : ActSelectAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_select_account)

        val mAccountManager = AccountManager.get(applicationContext)
        val availableAccountsList = mAccountManager.getAccountsByType(ACCOUNT_TYPE)

        val adapter: AccountListAdapter = AccountListAdapter(this, availableAccountsList, this)
        binding.lvAvailableAccounts.adapter = adapter
    }

    override fun onItemClicked(selectedAccount: Account) {
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, selectedAccount.name)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, selectedAccount.type)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}