package org.xwiki.android.sync.activities.Notifications

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.Credentials
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.SyncSettingsActivity
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.resolveApiManager
import org.xwiki.android.sync.rest.BaseApiManager
import org.xwiki.android.sync.rest.XWikiServices
import org.xwiki.android.sync.userAccountsRepo
import rx.Scheduler
import rx.schedulers.Schedulers
import java.net.URLEncoder

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
//        initApi()
        loadNotifications()
    }

    fun initApi() {
        appCoroutineScope.launch {
            val userAccount =
                userAccountsRepo.findByAccountName("somenath1435") ?: return@launch
            apiManager = resolveApiManager(userAccount)
            loadNotifications()
        }
    }

    fun loadNotifications() {

        adapter.setNotificationList(SyncSettingsActivity().getNotificationsList())

//        val progressDialog = ProgressDialog(this)
//        progressDialog.setCancelable(false)
//        progressDialog.setMessage("Loading Notifications")
//        progressDialog.show()

//        val username: String = URLEncoder.encode("xwiki:XWiki.somenath1435","utf-8")
//        appCoroutineScope.launch {
//                        apiManager.xwikiServicesApi.getNofity(Credentials.basic("somenath1435", "password"))
//            apiManager.xwikiServicesApi.getNotify()
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(
//                    {
//                        progressDialog.dismiss()
//                        adapter.setNotificationList(it.notifications)
//                        it.notifications.forEach {
//                            Log.e("Nofity", it.document.toString() + it.type.toString())
//                        }
//                    },
//                    {
//                        progressDialog.dismiss()
//                        Log.e("Error", it.message)
//                    }
//                )
//        }
    }
}