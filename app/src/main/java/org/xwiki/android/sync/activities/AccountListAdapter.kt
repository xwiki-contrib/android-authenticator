package org.xwiki.android.sync.activities

import android.accounts.Account
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.utils.AccountClickListener

class AccountListAdapter (
    private val mContext: Context,
    private var availableAccounts : Array<Account>,
    private val listener : AccountClickListener)
    : BaseAdapter()  {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val viewHolder: AccountListViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(mContext)
            convertView = inflater.inflate(R.layout.account_list_layout, null)
            viewHolder = AccountListViewHolder(convertView!!)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as AccountListViewHolder
        }

        val account = getItem(position)
        viewHolder.tvAccountName.text = account.name
        viewHolder.tvAccountType.text = account.type

        viewHolder.llAccountItem.setOnClickListener {
            listener.onItemClicked(account)
        }

        return convertView
    }

    override fun getItem(position: Int): Account {
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
    val tvAccountType : TextView
    val llAccountItem : LinearLayout

    init {
        tvAccountName = view.findViewById(R.id.tvAccountName)
        tvAccountType = view.findViewById(R.id.tvAccountType)
        llAccountItem = view.findViewById(R.id.llAccountItem)
    }
}