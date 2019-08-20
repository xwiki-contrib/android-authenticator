package org.xwiki.android.sync.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.xwiki.android.sync.contactdb.UserAccountId


class SyncSettingsViewModelFactory(
    private val application: Application,
    private val id: UserAccountId
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            (SyncSettingsViewModel::class.java) -> SyncSettingsViewModel(application, id) as T
            else -> throw UnsupportedOperationException("Can't create instance which is not type or subtype of SyncSettingsViewModel")
        }
    }
}
