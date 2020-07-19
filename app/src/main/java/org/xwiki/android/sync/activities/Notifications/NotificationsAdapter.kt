package org.xwiki.android.sync.activities.Notifications

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
        holder.title.text = "Document= " + notificationList[position].document
        holder.type.text = "Type= " + notificationList[position].type
    }

    fun setNotificationList(list: List<Notification>) {
        this.notificationList = list
        notifyDataSetChanged()
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val title: TextView = item.findViewById(R.id.notification_title)
        val type: TextView = item.findViewById(R.id.notification_type)
    }
}