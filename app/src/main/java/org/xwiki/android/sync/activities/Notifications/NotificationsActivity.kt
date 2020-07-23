package org.xwiki.android.sync.activities.Notifications

import android.accounts.Account
import android.accounts.AccountManager
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.xwiki.android.sync.R
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.resolveApiManager
import org.xwiki.android.sync.rest.BaseApiManager
import org.xwiki.android.sync.userAccountsRepo

class NotificationsActivity : AppCompatActivity() {

    private lateinit var apiManager: BaseApiManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        recyclerView = findViewById(R.id.recyclerView_notifications)
        adapter = NotificationsAdapter()
        recyclerView.adapter = adapter
        val extras = intent.extras

        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading Notifications")

        val currentUserAccountName = if (extras?.get("account") != null) {
            val intentAccount: Account = extras.get("account") as Account
            intentAccount.name
        } else {
            intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                ?: error("Can't get account name from intent - it is absent")
        }
        appCoroutineScope.launch {
            val userAccount =
                userAccountsRepo.findByAccountName(currentUserAccountName) ?: return@launch
            apiManager = resolveApiManager(userAccount)
            runOnUiThread { progressDialog.show() }
            val userId = "xwiki" + ":" + "XWiki" + "." + currentUserAccountName
//            apiManager.xwikiServicesApi.getNotify()
            apiManager.xwikiServicesApi.getNotify(userId,true)
                .subscribe(
                    {
                        runOnUiThread {
                            progressDialog.dismiss()
                            adapter.setNotificationList(it.notifications)
                        }
                        it.notifications.forEach {
                            Log.e(
                                "NotificationActivity",
                                it.document.toString() + it.type.toString()
                            )
                        }
                    },
                    {
                        runOnUiThread {
                            progressDialog.dismiss()
                        }
                        Log.e("Error", it.message)
                    }
                )
        }
    }
}