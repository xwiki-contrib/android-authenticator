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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xwiki.android.sync.*
import org.xwiki.android.sync.ViewModel.SyncSettingsViewModel
import org.xwiki.android.sync.ViewModel.SyncSettingsViewModelFactory
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.SearchResults.CustomObjectsSummariesContainer
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.clearOldAccountContacts
import org.xwiki.android.sync.databinding.ActivitySyncSettingsBinding
import org.xwiki.android.sync.rest.BaseApiManager
import org.xwiki.android.sync.utils.GroupsListChangeListener
import org.xwiki.android.sync.utils.getAppVersionName
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

class SyncSettingsActivity : AppCompatActivity(), GroupsListChangeListener {

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
    private val groups: MutableList<XWikiGroup> = mutableListOf()

    /**
     * List of received all users.
     */
    private val allUsers: MutableList<ObjectSummary> = mutableListOf()

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

    private lateinit var userAccount : UserAccount

    private lateinit var syncSettingsViewModel: SyncSettingsViewModel

    private lateinit var apiManager: BaseApiManager

    private var selectedStrings = ArrayList<String>()

    private lateinit var context: LifecycleOwner

    private lateinit var layoutManager: LinearLayoutManager

    private var isLoading = false

    private var initialUsersListLoading = true

    private var currentPage = 0

    private var lastVisiblePosition = 0

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private lateinit var dataSavingCheckbox: MenuItem

    /**
     * Init all views and other activity objects
     *
     * @param savedInstanceState
     *
     * @since 1.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sync_settings)

        binding.versionCheck.text = String.format(
            getString(R.string.versionTemplate),
            getAppVersionName(this)
        )
        val extras = intent.extras

        currentUserAccountName = if (extras ?.get("account") != null) {
            val intentAccount : Account = extras.get("account") as Account
            intentAccount.name
        } else {
            intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) ?: error("Can't get account name from intent - it is absent")
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mGroupAdapter = GroupListAdapter(groups, this)
        mUsersAdapter = UserListAdapter(allUsers, this)
        layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        binding.recyclerView.adapter = mUsersAdapter
        binding.recyclerView.addOnScrollListener(recyclerViewOnScrollListener)

        binding.selectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.syncTypeGetErrorContainer.visibility = View.GONE

                when(position) {
                    0 -> {
                        if (allUsers.isEmpty() && allUsersAreLoading) {
                            binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                        }
                    }
                    1 -> {
                        if (groups.isEmpty()) {
                            binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                        }
                    }
                    2 -> {
                        binding.syncTypeGetErrorContainer.visibility = View.GONE
                    }
                }

                if (userAccount.syncType == position) {
                    binding.nextButton.alpha = 0.8F
                } else {
                    binding.nextButton.alpha = 1F
                }
                chosenSyncType = position
                initialUsersListLoading = true
                currentPage = 0
                updateListView(false)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        binding.rvChangeSelectedAccount.setOnClickListener {
            val intent : Intent = Intent (this, SelectAccountActivity::class.java)
            startActivityForResult(intent, 1000)
        }
        binding.btTryAgain.setOnClickListener {
            appCoroutineScope.launch {
                when(chosenSyncType) {
                    SYNC_TYPE_ALL_USERS -> {loadAllUsers()}
                    SYNC_TYPE_SELECTED_GROUPS -> {loadSyncGroups()}
                }
            }
        }
        binding.versionCheck.setOnClickListener { v -> openAppMarket(v.context) }

        binding.nextButton.setOnClickListener { syncSettingComplete(it) }

        initData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sync_setting_view_menu, menu)
        dataSavingCheckbox = menu!!.findItem(R.id.action_data_saving)

        if (appContext.dataSaverModeEnabled) {
            dataSavingCheckbox.setChecked(true)
        } else {
            dataSavingCheckbox.setChecked(false)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_data_saving -> {
                if (item.isChecked) {
                    item.setChecked(false)
                    appContext.dataSaverModeEnabled = false
                } else {
                    item.setChecked(true)
                    appContext.dataSaverModeEnabled = true
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val totalItemCount = layoutManager.itemCount

                if (!isLoading && layoutManager.findLastCompletelyVisibleItemPosition() >= totalItemCount/2) {
                    lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                    loadMoreUsers()
                }
            }
        }
    }

    // TODO:: Test case for pagination of loading MoreUsers
    private fun loadMoreUsers () {
        isLoading = true
        showLoadMoreProgressBar()
        apiManager.xwikiServicesApi.getAllUsersListByOffset(currentPage, PAGE_SIZE)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                Action1 {
                    if (it.objectSummaries.isNotEmpty()) {
                        currentPage += PAGE_SIZE
                        allUsers.addAll(it.objectSummaries)
                        updateListView(true)

                        syncSettingsViewModel.updateAllUsersCache(
                            allUsers,
                            userAccount.id
                        )
                    }
                    initialUsersListLoading = false
                    allUsersAreLoading = false
                    isLoading = false
                    hideLoadMoreProgressBar()
                },
                Action1 {
                    allUsersAreLoading = false
                    isLoading = false
                    hideLoadMoreProgressBar()
                }
            )
    }

    fun scrollToCurrentPosition() {
        if (!initialUsersListLoading) {
            binding.recyclerView.scrollToPosition(lastVisiblePosition - 3)
        }
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
                    currentPage = 0
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
                binding.shimmerSyncUsers.visibility=View.VISIBLE
                binding.shimmerSyncUsers.startShimmer()
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.shimmerSyncUsers.stopShimmer()
                binding.shimmerSyncUsers.visibility=View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun showShimmer() {
        runOnUiThread{
            binding.shimmerSyncUsers.visibility=View.VISIBLE
            binding.shimmerSyncUsers.startShimmer()
            binding.syncTypeGetErrorContainer.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
        }
    }

    //this progress bar appears when more data is loaded into recycler view
    private fun showLoadMoreProgressBar() {
        runOnUiThread {
            binding.loadMoreProgressBar.visibility = View.VISIBLE
        }
    }

    private fun hideLoadMoreProgressBar() {
        runOnUiThread {
            binding.loadMoreProgressBar.visibility = View.INVISIBLE
        }
    }

    private fun hideShimmer() {
        runOnUiThread {
            binding.shimmerSyncUsers.stopShimmer()
            binding.shimmerSyncUsers.visibility=View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Load data to groups and all users lists.
     *
     * @since 0.4
     */
    private fun initData() {
        if (!intent.getBooleanExtra("Test", false)) {
            showShimmer()
        }

        appCoroutineScope.launch {
            userAccount = userAccountsRepo.findByAccountName(currentUserAccountName) ?: return@launch
            chosenSyncType = userAccount.syncType
            apiManager = resolveApiManager(userAccount)

            selectedStrings.clear()
            selectedStrings = userAccount.selectedGroupsList as ArrayList<String>

            withContext(Dispatchers.Main) {
                binding.tvSelectedSyncAcc.text = userAccount.accountName
                binding.tvSelectedSyncType.text = userAccount.serverAddress
                syncSettingsViewModel = ViewModelProviders.of(
                    this@SyncSettingsActivity,
                    SyncSettingsViewModelFactory (application)
                ).get(SyncSettingsViewModel::class.java)

                chosenSyncType = userAccount.syncType
            }
            initSyncList()
        }
    }

    // TODO:: Test case for both allUsers and SyncGroups
    private fun initSyncList () {
        loadSyncGroups()
        loadAllUsers()
    }

    // Initial loading of all users.
    private fun loadAllUsers() {
        allUsersAreLoading = true
        val users = syncSettingsViewModel.getAllUsersCache(userAccount.id) ?: emptyList()
        if (users.isEmpty()) {
            showShimmer()
        } else {
            allUsers.clear()
            allUsers.addAll(users)
            updateListView(false)
        }
        apiManager.xwikiServicesApi.getAllUsersListByOffset( currentPage, PAGE_SIZE)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { summaries ->
                    val objectSummary = summaries.objectSummaries
                    if (objectSummary.isNullOrEmpty()) {
                        runOnUiThread {
                            binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                            hideShimmer()
                        }
                    } else {
                        currentPage = PAGE_SIZE
                        runOnUiThread {
                            binding.syncTypeGetErrorContainer.visibility = View.GONE
                        }
                        allUsersAreLoading = false
                        allUsers.clear()
                        allUsers.addAll(summaries.objectSummaries)

                        syncSettingsViewModel.updateAllUsersCache(
                            summaries.objectSummaries,
                            userAccount.id
                        )
                        updateListView(true)
                    }
                },
                {
                    allUsersAreLoading = false
                    runOnUiThread {
                        Toast.makeText(
                            this@SyncSettingsActivity,
                            R.string.cantGetAllUsers,
                            Toast.LENGTH_SHORT
                        ).show()
                        if (allUsers.size <= 0) {
                            binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                        }
                    }
                    hideShimmer()
                }
            )
    }

    private fun loadSyncGroups() {
        val groupsCache = syncSettingsViewModel.getGroupsCache(userAccount.id) ?: emptyList()
        if (groupsCache.isEmpty()) {
            showShimmer()
        } else {
            groups.clear()
            groups.addAll(groupsCache)
            updateListView(false)
        }
        groupsAreLoading = true
        apiManager.xwikiServicesApi.availableGroups(
            LIMIT_MAX_SYNC_USERS
        )
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { xWikiGroupCustomSearchResultContainer ->
                    groupsAreLoading = false
                    val searchResults = xWikiGroupCustomSearchResultContainer.searchResults
                    hideShimmer()

                    if (searchResults.isNullOrEmpty()) {
                        runOnUiThread {
                            if (chosenSyncType == SYNC_TYPE_SELECTED_GROUPS) {
                                binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        runOnUiThread {
                            binding.syncTypeGetErrorContainer.visibility = View.GONE
                        }
                        groups.clear()
                        groups.addAll(searchResults)
                        syncSettingsViewModel.updateGroupsCache(
                            searchResults,
                            userAccount.id
                        )
                        updateListView(false)
                    }
                },
                {
                    groupsAreLoading = false
                    runOnUiThread {
                        Toast.makeText(
                            this@SyncSettingsActivity,
                            R.string.cantGetGroups,
                            Toast.LENGTH_SHORT
                        ).show()
                        if (groups.size <= 0) {
                            binding.syncTypeGetErrorContainer.visibility = View.VISIBLE
                        }
                    }
                    hideShimmer()
                }
            )
    }

    // Load all users at once, does not support pagination.
    private fun updateSyncAllUsers() {
        val users = syncSettingsViewModel.getAllUsersCache(userAccount.id) ?: emptyList()
        if (users.isEmpty()) {
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

                        syncSettingsViewModel.updateAllUsersCache(
                            summaries.objectSummaries,
                            userAccount.id
                        )
                        updateListView(true)
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
                        hideShimmer()
                    }
                )
        } else {
            allUsers.clear()
            allUsers.addAll(users)
            updateListView(false)
        }
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
        appCoroutineScope.launch(Dispatchers.Main) {
            if (syncNothing()) {
                binding.recyclerView.visibility = View.GONE
                binding.shimmerSyncUsers.stopShimmer()
                binding.shimmerSyncUsers.visibility=View.GONE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                if (syncGroups()) {
                    binding.recyclerView.adapter = mGroupAdapter
                    mGroupAdapter.refresh(groups, userAccount.selectedGroupsList)
                } else {
                    binding.recyclerView.adapter = mUsersAdapter
                    mUsersAdapter.refresh(allUsers)
                }
                binding.recyclerView.layoutManager?.scrollToPosition(0)
                mUsersAdapter.refresh(allUsers)
                if (hideProgressBar) {
                    hideShimmer()
                }
            }
        }
    }

    /**
     * Save settings of synchronization.
     */
    fun syncSettingComplete(v: View) {
        val oldSyncType = userAccount.syncType
        if (oldSyncType == chosenSyncType && !syncGroups()) {
            binding.nextButton.alpha = 0.8F
            Toast.makeText(this, "Nothing has changed since your last sync", Toast.LENGTH_SHORT).show()
            return
        }

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
            userAccount.syncType = SYNC_TYPE_NO_NEED_SYNC
            userAccount.selectedGroupsList = mutableListOf()
            userAccount.let { syncSettingsViewModel.updateUser(it) }
            setSync(false)
            finish()
        } else if (syncAllUsers()) {
            userAccount.syncType = SYNC_TYPE_ALL_USERS
            userAccount.selectedGroupsList = mutableListOf()
            userAccount.let { syncSettingsViewModel.updateUser(it) }
            setSync(true)
            finish()
        } else if (syncGroups()) {
            //compare to see if there are some changes.
            if (oldSyncType == chosenSyncType && compareSelectGroups()) {
                Toast.makeText(this, "Nothing has changed since your last sync", Toast.LENGTH_SHORT).show()
                return
            }

            userAccount.selectedGroupsList.clear()
            userAccount.selectedGroupsList.addAll(mGroupAdapter.saveSelectedGroups())

            userAccount.syncType = SYNC_TYPE_SELECTED_GROUPS

            appCoroutineScope.launch {
                userAccountsRepo.updateAccount(userAccount)
                setSync(true)
                finish()
            }
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
        val oldList = userAccount.selectedGroupsList
        if (newList.isEmpty() && oldList.isEmpty()) {
            return false
        }
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
    }

    override fun onChangeListener() {
        if (compareSelectGroups()) {
            binding.nextButton.alpha = 0.8F
        } else {
            binding.nextButton.alpha = 1F
        }
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerSyncUsers.startShimmer()
    }

    override fun onPause() {
        binding.shimmerSyncUsers.stopShimmer()
        super.onPause()
    }
}
