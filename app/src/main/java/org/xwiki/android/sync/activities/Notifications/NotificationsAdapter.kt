package org.xwiki.android.sync.activities.Notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.xwiki.android.sync.R
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.bean.XWikiUserFull
import org.xwiki.android.sync.bean.notification.Notification
import org.xwiki.android.sync.rest.BaseApiManager

class NotificationsAdapter(
    private val context: Context
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    private var notificationList: List<Notification> = listOf()

    private lateinit var apiManager: BaseApiManager

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = notificationList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val htmlStr = notificationList[position].html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.html.setText(Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_COMPACT))
        } else holder.html.setText(Html.fromHtml(htmlStr))

        holder.ll.setOnClickListener {
            val doc = notificationList[position].document
            if (!doc.isNullOrEmpty()) {
                Log.e("NotificationAdapter", doc)
                if (!doc.isNullOrEmpty()) {
                    getPageDetails(doc, it)
                }
            }
        }
    }

    fun setNotificationList(list: List<Notification>) {
        this.notificationList = list
        notifyDataSetChanged()
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val html: TextView = item.findViewById(R.id.notification_html)
        val ll: LinearLayout = item.findViewById(R.id.notification_linear_layout)
    }

    fun setApiManager(baseApiManager: BaseApiManager) {
        this.apiManager = baseApiManager
    }

    fun getPageDetails(document: String, view: View) {
        val splittedDocument = XWikiUserFull.splitDocument(document)
        Log.e("Splitted Document", splittedDocument)

        appCoroutineScope.launch {
            apiManager.xwikiServicesApi.getPageDetails(splittedDocument)
                .subscribe(
                    {
                        Log.e("XwikiAbsoluteUrl", it.xwikiAbsoluteUrl+"")
                        context.startNotificationWebViewActivity(it.xwikiAbsoluteUrl ?: return@subscribe)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        }
    }
}