package org.xwiki.android.sync.activities.Notifications

import android.accounts.Account
import android.accounts.AccountManager
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.xwiki.android.sync.R
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.bean.notification.Notification
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
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.notification_toolbar)
        if (toolbar != null)
            setSupportActionBar(toolbar)
        recyclerView = findViewById(R.id.recyclerView_notifications)
        adapter = NotificationsAdapter(applicationContext)
        recyclerView.adapter = adapter
        val extras = intent.extras

        val emptyNotifications = findViewById<TextView>(R.id.empty_notification_textview)
        emptyNotifications.visibility = View.GONE

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
            adapter.setApiManager(apiManager)
            runOnUiThread { progressDialog.show() }
            val userId = "xwiki" + ":" + "XWiki" + "." + currentUserAccountName

            apiManager.xwikiServicesApi.getNotify(userId, true, true)
                .subscribe(
                    {
                        var notificationList: List<Notification>? = null
                        if(!it.notifications.isNullOrEmpty()) notificationList=it.notifications
                        else if (it.asyncId != null) {
                            do {
                                var id = it.asyncId
                                if (id != null) {
                                    apiManager.xwikiServicesApi.getNotifyAsync(userId, true, true, id)
                                        .subscribe(
                                            {
                                                Log.e("AsyncId", it.asyncId.toString())
                                                Log.e("AsyncId", it.notifications.toString())
                                                id = it.asyncId
                                                notificationList = it.notifications
                                            },
                                            {
                                                it.printStackTrace()
                                            }
                                        )
                                }
                            } while (id != null)
                        }

                        Log.e("FinalList", notificationList.toString())
                        val notifications = notificationList
                        if (!notifications.isNullOrEmpty()) {
                            runOnUiThread {
                                progressDialog.dismiss()
                                if (notifications.isNullOrEmpty())
                                    emptyNotifications.visibility = View.VISIBLE
                                else
                                    adapter.setNotificationList(notifications)
                            }
                            notifications.forEach {
                                Log.e(
                                    "NotificationActivity",
                                    it.document.toString() + it.type.toString()
                                )
                            }
                        }
                    },
                    {
                        runOnUiThread {
                            progressDialog.dismiss()
                            emptyNotifications.visibility = View.VISIBLE
                        }
                        Log.e("Error", it.message)
                    }
                )
        }
    }
}