package org.xwiki.android.sync.activities.Notifications

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.bean.notification.Notification

class NotificationsAdapter : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    private var notificationList: List<Notification> = listOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = notificationList.size

    override fun onBindViewHolder(holder: NotificationsAdapter.ViewHolder, position: Int) {
//        holder.title.text = "Document= " + notificationList[position].document
//        holder.type.text = "Type= " + notificationList[position].type
        val htmlStr = notificationList[position].html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.html.setText(Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_COMPACT))
        }
        else holder.html.setText(Html.fromHtml(htmlStr))
    }

    fun setNotificationList(list: List<Notification>) {
        this.notificationList = list
        notifyDataSetChanged()
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val title: TextView = item.findViewById(R.id.notification_title)
        val type: TextView = item.findViewById(R.id.notification_type)
        val html : TextView = item.findViewById(R.id.notification_html)
    }
}