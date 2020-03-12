package org.xwiki.android.sync.activities

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.base.BaseActivity
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.databinding.ActSelectAccountBinding
import org.xwiki.android.sync.userAccountsRepo
import org.xwiki.android.sync.utils.AccountClickListener

class SelectAccountActivity : BaseActivity(), AccountClickListener {

    lateinit var binding: ActSelectAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_select_account)

        appCoroutineScope.launch {
            val adapter = AccountListAdapter(
                this@SelectAccountActivity,
                userAccountsRepo.getAll(),
                this@SelectAccountActivity
            )
            val recyclerView = binding.lvAvailableAccounts
            recyclerView.layoutManager = LinearLayoutManager(this@SelectAccountActivity)
            recyclerView.adapter = adapter
        }
    }

    override fun invoke(selectedAccount: UserAccount) {
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, selectedAccount.accountName)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, selectedAccount.serverAddress)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}