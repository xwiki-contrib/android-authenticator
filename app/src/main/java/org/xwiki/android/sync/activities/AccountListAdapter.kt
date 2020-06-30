package org.xwiki.android.sync.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.xwiki.android.sync.R
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.utils.AccountClickListener

class AccountListAdapter(
    private val mContext: Context,
    private var availableAccounts: List<UserAccount>,
    private val listener: AccountClickListener
) : RecyclerView.Adapter<AccountListAdapter.AccountListViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = AccountListViewHolder(LayoutInflater.from(mContext), listener)

    override fun getItemCount(): Int = availableAccounts.size

    override fun onBindViewHolder(holder: AccountListViewHolder, position: Int) {
        holder.account = availableAccounts[position]
    }

    class AccountListViewHolder(
        layoutInflater: LayoutInflater,
        listener: AccountClickListener
    ) : RecyclerView.ViewHolder(
        layoutInflater.inflate(R.layout.account_list_layout, null)
    ) {
        private val tvAccountName: TextView = itemView.findViewById(R.id.tvAccountName)
        private val tvAccountServerAddress: TextView = itemView.findViewById(R.id.tvAccountServerAddress)

        var account: UserAccount? = null
            set(value) {
                field = value
                tvAccountName.text = account ?.accountName ?: ""
                tvAccountServerAddress.text = account ?.serverAddress ?: ""
            }

        init {
            itemView.findViewById<View>(R.id.llAccountItem).setOnClickListener {
                account ?.also { listener(it) }
            }
        }
    }
}

