package org.xwiki.android.sync.activities.OIDC

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

class OIDCAccountAdapter (
    private val mContext: Context,
    private var availableAccounts : List<UserAccount>,
    private val listener : AccountClickListener
) : BaseAdapter()  {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?:let {
            val inflater = LayoutInflater.from(mContext)
            inflater.inflate(R.layout.oidc_account_list_layout, null)
        }

        val viewHolder = AccountListViewHolder(view)
        view.tag = viewHolder

        val account = getItem(position)
        viewHolder.tvOIDCAccountName.text = account.accountName
        viewHolder.tvOIDCAccountServerAddress.text = account.serverAddress

        viewHolder.llOIDCAccountItem.setOnClickListener {
            listener(account)
        }

        return view
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
    val tvOIDCAccountName : TextView = view.findViewById(R.id.tvOIDCAccountName)
    val llOIDCAccountItem : LinearLayout = view.findViewById(R.id.llOIDCAccountItem)
    val tvOIDCAccountServerAddress: TextView = view.findViewById(R.id.tvOIDCAccountServerAddress)
}