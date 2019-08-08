package org.xwiki.android.sync.activities.OIDC

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

class OIDCAccountAdapter (
    private val mContext: Context,
    private var availableAccounts : Array<Account>,
    private val listener : AccountClickListener
) : BaseAdapter()  {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var viewHolder: AccountListViewHolder

        view.let {
            if (it == null) {
                val inflater = LayoutInflater.from(mContext)
                view = inflater.inflate(R.layout.oidc_account_list_layout, null)
                viewHolder = AccountListViewHolder(view!!)
                it?.tag = viewHolder
            } else {
                viewHolder = AccountListViewHolder(view!!)
                it.tag = viewHolder
                viewHolder = view?.tag as AccountListViewHolder
            }
            val account = getItem(position)
            viewHolder.tvOIDCAccountName.text = account.name

            viewHolder.llOIDCAccountItem.setOnClickListener {
                listener(account)
            }
        }

        return view!!
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
    val tvOIDCAccountName : TextView
    val llOIDCAccountItem : LinearLayout

    init {
        tvOIDCAccountName = view.findViewById(R.id.tvOIDCAccountName)
        llOIDCAccountItem = view.findViewById(R.id.llOIDCAccountItem)
    }
}