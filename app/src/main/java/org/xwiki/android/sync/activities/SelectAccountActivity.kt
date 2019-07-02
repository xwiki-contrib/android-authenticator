package org.xwiki.android.sync.activities

import android.accounts.AccountManager
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
        binding.rvAvailableAccounts.adapter = adapter
        binding.rvAvailableAccounts.layoutManager = LinearLayoutManager(this)

    }

    override fun onItemClicked(accountName: String?) {
        val syncActivityIntent = Intent(this, SyncSettingsActivity::class.java)
        syncActivityIntent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
        startActivity(
            syncActivityIntent
        )
        finish()
    }
}