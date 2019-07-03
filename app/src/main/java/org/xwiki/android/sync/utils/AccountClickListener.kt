package org.xwiki.android.sync.utils

import android.accounts.Account

interface AccountClickListener {
    fun onItemClicked (selectedAccount : Account)
}