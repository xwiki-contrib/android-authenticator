package org.xwiki.android.sync.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.xwiki.android.sync.*
import org.xwiki.android.sync.ViewModel.SyncSettingsViewModel
import org.xwiki.android.sync.activities.base.BaseActivity
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.SerachResults.CustomObjectsSummariesContainer
import org.xwiki.android.sync.bean.SerachResults.CustomSearchResultContainer
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.contactdb.*
import org.xwiki.android.sync.databinding.ActivitySyncSettingsBinding
import org.xwiki.android.sync.utils.decrement
import org.xwiki.android.sync.utils.getAppVersionName
import org.xwiki.android.sync.utils.increment
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.util.*


/**
 * Tag which will be used for logging.
 */
private val TAG = SyncSettingsActivity::class.java.simpleName

/**
 * Open market with application page.
 *
 * @param context Context to know where from to open market
 */
private fun openAppMarket(context: Context) {
    val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.packageName))
    var marketFound = false
    // find all applications able to handle our rateIntent
    val otherApps = context.packageManager.queryIntentActivities(rateIntent, 0)
    for (otherApp in otherApps) {
        // look for Google Play application
        if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {
            val otherAppActivity = otherApp.activityInfo
            val componentName = ComponentName(
                otherAppActivity.applicationInfo.packageName,
                otherAppActivity.name
            )
            rateIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            rateIntent.component = componentName
            context.startActivity(rateIntent)
            marketFound = true
            break
        }
    }
    // if GooglePlay not present on device, open web browser
    if (!marketFound) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
        )
        context.startActivity(webIntent)
    }
}

class SyncSettingsActivity : BaseActivity() {

    /**
     * DataBinding for accessing layout variables.
     */
    lateinit var binding : ActivitySyncSettingsBinding

    /**
     * Adapter for groups
     */
    private lateinit var mGroupAdapter: GroupListAdapter

    /**
     * Adapter for users.
     */
    private lateinit var mUsersAdapter: UserListAdapter

    /**
     * List of received groups.
     */
    private lateinit var groups: MutableList<XWikiGroup>

    /**
     * List of received all users.
     */
    private lateinit var allUsers: MutableList<ObjectSummary>

    /**
     * Currently chosen sync type.
     */
    private var chosenSyncType: Int? = SYNC_TYPE_NO_NEED_SYNC

    /**
     * Flag of currently loading groups.
     */
    @Volatile
    private var groupsAreLoading: Boolean = false

    /**
     * Flag of currently loading all users.
     */
    @Volatile
    private var allUsersAreLoading: Boolean = false

    private lateinit var currentUserAccountName : String

    private lateinit var currentUserAccountType : String

    private var user : User? = null

    private lateinit var syncSettingsViewModel: SyncSettingsViewModel

    private var selectedStrings = ArrayList<String>()

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Init all views and other activity objects
     *
     * @param savedInstanceState
     *
     * @since 1.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sync_settings)

        binding.versionCheck.text = String.format(
            getString(R.string.versionTemplate),
            getAppVersionName(this)
        )

        if (intent.extras.get("account") != null) {
            val intentAccount : Account = intent.extras.get("account") as Account
            currentUserAccountName = intentAccount.name
            currentUserAccountType = intentAccount.type
        } else {
            currentUserAccountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            currentUserAccountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        }

        groups = ArrayList()
        allUsers = ArrayList()
        mGroupAdapter = GroupListAdapter(groups)
        mUsersAdapter = UserListAdapter(allUsers)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = mUsersAdapter

        binding.selectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                chosenSyncType = position
                updateListView(false)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        binding.rvChangeSelectedAccount.setOnClickListener {
            val intent : Intent = Intent (this, SelectAccountActivity::class.java)
            startActivityForResult(intent, 1000)
        }
        binding.btTryAgain.setOnClickListener {
            initData()
        }
        binding.versionCheck.setOnClickListener { v -> openAppMarket(v.context) }

        chosenSyncType = user?.syncType
        chosenSyncType?.let { binding.selectSpinner.setSelection(it) }
        syncSettingsViewModel = ViewModelProviders.of(this).get(SyncSettingsViewModel::class.java)

        initData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    currentUserAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    currentUserAccountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)

                    binding.tvSelectedSyncAcc.text = currentUserAccountName
                    binding.tvSelectedSyncType.text = currentUserAccountType
                    initData()
                }
            }
        }

    }

    /**
     * Show progress bar if need or hide otherwise.
     *
     * @since 0.4.2
     */
    private fun refreshProgressBar() {
        val progressBarVisible = syncGroups() && groupsAreLoading || syncAllUsers() && allUsersAreLoading
        runOnUiThread {
            if (progressBarVisible) {
                binding.listViewProgressBar.visibility = View.VISIBLE
                binding.settingsSyncListViewContainer.visibility = View.GONE
            } else {
                binding.listViewProgressBar.visibility = View.GONE
                binding.settingsSyncListViewContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun showProgressBar() {
        runOnUiThread {
            binding.listViewProgressBar.visibility = View.VISIBLE
            binding.settingsSyncListViewContainer.visibility = View.GONE
        }
    }

    private fun hideProgressBar() {
        runOnUiThread {
            binding.listViewProgressBar.visibility = View.GONE
            binding.settingsSyncListViewContainer.visibility = View.VISIBLE
        }
    }

    /**
     * Load data to groups and all users lists.
     *
     * @since 0.4
     */
    private fun initData() {
        binding.tvSelectedSyncAcc.text = currentUserAccountName
        binding.tvSelectedSyncType.text = currentUserAccountType
        showProgressBar()

        syncSettingsViewModel.getUser(currentUserAccountName).observe( this, Observer {
            if (it != null) {
                user = it
                selectedStrings.clear()
                selectedStrings = user?.selectedGroupsList as ArrayList<String>
                user?.syncType?.let {
                    if (it >= 0) {
                        chosenSyncType = it
                        binding.selectSpinner.setSelection(it)
                    }
                }

                currentBaseUrl(it.accountName)
                user?.syncType?.let { it1 -> setUserSyncType(it1) }
                user?.cookie?.let { it1 -> setUserCookie(it1) }

                getSyncList()
            }
        })
    }

    private fun getSyncList () {

        syncSettingsViewModel.getSyncTypeGroupsList()?.observe(this, Observer {
            if (it.size == 0) {
                increment()
                groupsAreLoading = true
                apiManager.xwikiServicesApi.availableGroups(
                    LIMIT_MAX_SYNC_USERS
                )
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        Action1<CustomSearchResultContainer<XWikiGroup>> { xWikiGroupCustomSearchResultContainer ->
                            groupsAreLoading = false
                            runOnUiThread {
                                binding.syncTypeGetErrorContainer.visibility = View.GONE
                            }
                            val searchResults = xWikiGroupCustomSearchResultContainer.searchResults
                            if (searchResults != null) {
                                groups.clear()
                                groups.addAll(searchResults)
                                syncSettingsViewModel.insertSyncTypeGroupsList(SyncTypeGroupsList(0, searchResults))
                                updateListView(false)
                            }
                        },
                        Action1<Throwable> {
                            groupsAreLoading = false
                            runOnUiThread {
                                Toast.makeText(
                                    this@SyncSettingsActivity,
                                    R.string.cantGetGroups,
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                            }
                            hideProgressBar()
                            decrement()
                        }
                    )
            } else {
                groups.clear()
                groups.addAll(it[0].groupsList)
                updateListView(false)
            }
        })

        syncSettingsViewModel.getSyncTypeAllUsersList()?.observe(this, Observer {
            if (it.size == 0) {
                allUsersAreLoading = true
                apiManager.xwikiServicesApi.allUsersPreview
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        Action1<CustomObjectsSummariesContainer<ObjectSummary>> { summaries ->
                            runOnUiThread {
                                binding.syncTypeGetErrorContainer.visibility = View.GONE
                            }
                            allUsersAreLoading = false
                            allUsers.clear()
                            allUsers.addAll(summaries.objectSummaries)

                            syncSettingsViewModel.insertSyncTypeAllUsersList(SyncTypeAllUsersList(0, summaries.objectSummaries))
                            updateListView(true)
                            decrement()
                        },
                        Action1<Throwable> {
                            allUsersAreLoading = false
                            runOnUiThread {
                                Toast.makeText(
                                    this@SyncSettingsActivity,
                                    R.string.cantGetAllUsers,
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                            }
                            hideProgressBar()
                            decrement()
                        }
                    )
            } else {
                allUsers.clear()
                allUsers.addAll(it[0].allUsersList)
                updateListView(false)
            }

        })
    }

    /**
     * @return true if currently selected to sync groups or false otherwise
     */
    private fun syncGroups(): Boolean {
        return chosenSyncType == SYNC_TYPE_SELECTED_GROUPS
    }

    /**
     * @return true if currently selected to sync all users or false otherwise
     */
    private fun syncAllUsers(): Boolean {
        return chosenSyncType == SYNC_TYPE_ALL_USERS
    }

    /**
     * @return true if currently selected to sync not or false otherwise
     */
    private fun syncNothing(): Boolean {
        return chosenSyncType == SYNC_TYPE_NO_NEED_SYNC
    }

    /**
     * Update list view and hide/show view from [.getListViewContainer]
     */
    private fun updateListView(hideProgressBar: Boolean) {
        if (syncNothing()) {
            binding.settingsSyncListViewContainer.visibility = View.GONE
            binding.listViewProgressBar.visibility = View.GONE
        } else {
            binding.settingsSyncListViewContainer.visibility = View.VISIBLE
            if (syncGroups()) {
                binding.recyclerView.adapter = mGroupAdapter
                mGroupAdapter.refresh(groups, user?.selectedGroupsList)
            } else {
                binding.recyclerView.adapter = mUsersAdapter
                mUsersAdapter.refresh(allUsers)
            }
            mUsersAdapter.refresh(allUsers)
            if (hideProgressBar) {
                hideProgressBar()
            }
        }
    }

    /**
     * Save settings of synchronization.
     */
    fun syncSettingComplete(v: View) {
        val oldSyncType = user?.syncType
        if (oldSyncType == chosenSyncType && !syncGroups()) {
            return
        }

        //TODO:: fix when will separate to different accounts
        val mAccountManager = AccountManager.get(applicationContext)
        val availableAccounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE)
        var account : Account = availableAccounts[0]
        for (acc in availableAccounts) {
            if (acc.name.equals(currentUserAccountName)) {
                account = acc
            }
        }

        clearOldAccountContacts(
            contentResolver,
            account
        )

        //if has changes, set sync
        if (syncNothing()) {
            user?.syncType = SYNC_TYPE_NO_NEED_SYNC
            user?.let { syncSettingsViewModel.updateUser(it) }
            setSync(false)
            finish()
        } else if (syncAllUsers()) {
            user?.syncType = SYNC_TYPE_ALL_USERS
            user?.let { syncSettingsViewModel.updateUser(it) }
            setSync(true)
            finish()
        } else if (syncGroups()) {
            //compare to see if there are some changes.
            if (oldSyncType == chosenSyncType && compareSelectGroups()) {
                Toast.makeText(this, getString(R.string.unchangedSettings), Toast.LENGTH_LONG).show()
                return
            }

            user?.selectedGroupsList?.clear()
            user?.selectedGroupsList?.addAll(mGroupAdapter.saveSelectedGroups())

            user?.syncType = SYNC_TYPE_SELECTED_GROUPS

            scope.launch {
                val userDao = AppDatabase.getInstance(appContext).userDao()
                val userRepository = AppRepository(userDao, null, null)
                user?.let { userRepository.updateUser(it) }
            }
            setSync(true)
            finish()
            scope.cancel()
        }
    }

    /**
     * Enable/disable synchronization depending on syncEnabled.
     *
     * @param syncEnabled Flag to enable (if true) / disable (if false) synchronization
     */
    private fun setSync(syncEnabled: Boolean) {
        val mAccountManager = AccountManager.get(applicationContext)
        val availableAccounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE)
        var account : Account = availableAccounts[0]
        for (acc in availableAccounts) {
            if (acc.name.equals(currentUserAccountName)) {
                account = acc
            }
        }
        if (syncEnabled) {
            mAccountManager.setUserData(account, SYNC_MARKER_KEY, null)
            ContentResolver.cancelSync(account, ContactsContract.AUTHORITY)
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1)
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
            ContentResolver.addPeriodicSync(
                account,
                ContactsContract.AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL.toLong()
            )
            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
        } else {
            ContentResolver.cancelSync(account, ContactsContract.AUTHORITY)
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 0)
        }
    }

    /**
     * @return true if old list equal to new list of groups
     */
    private fun compareSelectGroups(): Boolean {
        //new
        val newList = mGroupAdapter.selectGroups
        //old
        val oldList = user?.selectedGroupsList
        if (newList == null && oldList == null) {
            return true
        } else if (newList != null && oldList != null) {
            if (newList.size != oldList.size) {
                return false
            } else {
                for (item in newList) {
                    if (!oldList.contains(item.id)) {
                        return false
                    }
                }
                return true
            }
        } else {
            return false
        }
    }
}
