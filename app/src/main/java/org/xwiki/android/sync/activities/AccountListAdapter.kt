package org.xwiki.android.sync.activities

import android.accounts.Account
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.utils.AccountClickListener

class AccountListAdapter (
    private val mContext: Context,
    private var availableAccounts : Array<Account>?,
    private val listener : AccountClickListener)
    : RecyclerView.Adapter<AccountListViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountListViewHolder {
        return AccountListViewHolder(LayoutInflater.from(mContext).inflate(R.layout.account_list_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return availableAccounts?.size!!
    }

    override fun onBindViewHolder(holder: AccountListViewHolder, position: Int) {
        holder.tvAccountName.text = availableAccounts?.get(position)?.name

        holder.llAccountItem.setOnClickListener {
            listener.onItemClicked(availableAccounts?.get(position)?.name)
        }
    }

}

class AccountListViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    val tvAccountName = view.findViewById<TextView>(R.id.tvAccountName)
    val llAccountItem = view.findViewById<LinearLayout>(R.id.llAccountItem)
}