package org.xwiki.android.sync.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.utils.AccountClickListener

class AccountListAdapter (
    private val mContext: Context,
    private var availableAccounts : List<UserAccount>,
    private val listener : AccountClickListener
) : BaseAdapter()  {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val viewHolder: AccountListViewHolder

        view.let {
            if (it == null) {
                val inflater = LayoutInflater.from(mContext)
                view = inflater.inflate(R.layout.account_list_layout, null)
                viewHolder = AccountListViewHolder(view!!)
                it?.tag = viewHolder
            } else {
                viewHolder = view?.tag as AccountListViewHolder
            }
            val account = getItem(position)
            viewHolder.tvAccountName.text = account.accountName
            viewHolder.tvAccountServerAddress.text = account.serverAddress

            viewHolder.llAccountItem.setOnClickListener {
                listener(account)
            }
        }

        return view!!
    }

    override fun getItem(position: Int): UserAccount {
        return availableAccounts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return availableAccounts.size
    }

}

private class AccountListViewHolder (view: View) {
    val tvAccountName : TextView
    val tvAccountServerAddress : TextView
    val llAccountItem : LinearLayout

    init {
        tvAccountName = view.findViewById(R.id.tvAccountName)
        tvAccountServerAddress = view.findViewById(R.id.tvAccountServerAddress)
        llAccountItem = view.findViewById(R.id.llAccountItem)
    }
}