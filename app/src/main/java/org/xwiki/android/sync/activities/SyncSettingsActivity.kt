package org.xwiki.android.sync.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.DataBindingUtil
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.Constants
import org.xwiki.android.sync.R
import org.xwiki.android.sync.activities.base.BaseActivity
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.SerachResults.CustomObjectsSummariesContainer
import org.xwiki.android.sync.bean.SerachResults.CustomSearchResultContainer
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.utils.SharedPrefsUtils
import org.xwiki.android.sync.utils.SystemTools
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.util.ArrayList
import org.xwiki.android.sync.contactdb.clearOldAccountContacts
import org.xwiki.android.sync.databinding.ActivitySyncSettingsBinding

class SyncSettingsActivity : BaseActivity() {

    /**
     * DataBinding for accessing layout variables.
     */
    var binding : ActivitySyncSettingsBinding? = null

    /**
     * Adapter for groups
     */
    private var mGroupAdapter: GroupListAdapter? = null

    /**
     * Adapter for users.
     */
    private var mUsersAdapter: UserListAdapter? = null

    /**
     * List of received groups.
     */
    private var groups: MutableList<XWikiGroup>? = null

    /**
     * List of received all users.
     */
    private var allUsers: MutableList<ObjectSummary>? = null

    /**
     * Currently chosen sync type.
     */
    private var SYNC_TYPE = Constants.SYNC_TYPE_NO_NEED_SYNC

    /**
     * Flag of currently loading groups.
     */
    @Volatile
    private var groupsAreLoading: Boolean? = false

    /**
     * Flag of currently loading all users.
     */
    @Volatile
    private var allUsersAreLoading: Boolean? = false

    /**
     * Init all views and other activity objects
     *
     * @param savedInstanceState
     *
     * @since 1.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sync_settings);

        binding!!.versionCheck.text = String.format(
            getString(R.string.versionTemplate),
            SystemTools.getAppVersionName(this)
        )
        binding!!.versionCheck.setOnClickListener { v -> openAppMarket(v.context) }

        binding!!.listView.emptyView = binding!!.syncTypeGetErrorContainer
        groups = ArrayList()
        allUsers = ArrayList()
        mGroupAdapter = GroupListAdapter(this, groups)
        mUsersAdapter = UserListAdapter(this, allUsers!!)
        initData(null)
        binding!!.listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        binding!!.selectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                SYNC_TYPE = position
                updateListView()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        SYNC_TYPE = SharedPrefsUtils.getValue(this, Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS)
        binding!!.selectSpinner.setSelection(SYNC_TYPE)
    }

    /**
     * Show progress bar if need or hide otherwise.
     *
     * @since 0.4.2
     */
    private fun refreshProgressBar() {
        val progressBarVisible = syncGroups() && groupsAreLoading!! || syncAllUsers() && allUsersAreLoading!!
        runOnUiThread {
            if (progressBarVisible) {
                binding!!.listViewProgressBar.visibility = View.VISIBLE
                binding!!.settingsSyncListViewContainer.visibility = View.GONE
            } else {
                binding!!.listViewProgressBar.visibility = View.GONE
                binding!!.settingsSyncListViewContainer.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Load data to groups and all users lists.
     *
     * @since 0.4
     */
    fun initData(v: View?) {
        if (groups!!.isEmpty()) {
            groupsAreLoading = true
            AppContext.getApiManager().xwikiServicesApi.availableGroups(
                Constants.LIMIT_MAX_SYNC_USERS
            )
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    Action1<CustomSearchResultContainer<XWikiGroup>> { xWikiGroupCustomSearchResultContainer ->
                        groupsAreLoading = false
                        val searchResults = xWikiGroupCustomSearchResultContainer.searchResults
                        if (searchResults != null) {
                            groups!!.clear()
                            groups!!.addAll(searchResults)
                            updateListView()
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
                        }
                        refreshProgressBar()
                    }
                )
        }
        if (allUsers!!.isEmpty()) {
            allUsersAreLoading = true
            AppContext.getApiManager().xwikiServicesApi.allUsersPreview
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    Action1<CustomObjectsSummariesContainer<ObjectSummary>> { summaries ->
                        allUsersAreLoading = false
                        allUsers!!.clear()
                        allUsers!!.addAll(summaries.objectSummaries!!)
                        updateListView()
                    },
                    Action1<Throwable> {
                        allUsersAreLoading = false
                        runOnUiThread {
                            Toast.makeText(
                                this@SyncSettingsActivity,
                                R.string.cantGetAllUsers,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        refreshProgressBar()
                    }
                )
        }
        if (allUsersAreLoading!! || groupsAreLoading!!) {
            refreshProgressBar()
        }
    }

    /**
     * @return true if currently selected to sync groups or false otherwise
     */
    private fun syncGroups(): Boolean {
        return SYNC_TYPE == Constants.SYNC_TYPE_SELECTED_GROUPS
    }

    /**
     * @return true if currently selected to sync all users or false otherwise
     */
    private fun syncAllUsers(): Boolean {
        return SYNC_TYPE == Constants.SYNC_TYPE_ALL_USERS
    }

    /**
     * @return true if currently selected to sync not or false otherwise
     */
    private fun syncNothing(): Boolean {
        return SYNC_TYPE == Constants.SYNC_TYPE_NO_NEED_SYNC
    }

    /**
     * Update list view and hide/show view from [.getListViewContainer]
     */
    private fun updateListView() {
        if (syncNothing()) {
            binding!!.settingsSyncListViewContainer.visibility = View.GONE
            binding!!.listViewProgressBar.visibility = View.GONE
        } else {
            binding!!.settingsSyncListViewContainer.visibility = View.VISIBLE
            val adapter: BaseAdapter?
            if (syncGroups()) {
                adapter = mGroupAdapter
                mGroupAdapter!!.refresh(groups!!)
            } else {
                adapter = mUsersAdapter
                mUsersAdapter!!.refresh(allUsers!!)
            }
            if (adapter !== binding!!.listView.adapter) {
                binding!!.listView.adapter = adapter
            }
            refreshProgressBar()
        }
    }

    /**
     * Save settings of synchronization.
     */
    fun syncSettingComplete(v: View) {
        //check changes. if no change, directly return
        val oldSyncType = SharedPrefsUtils.getValue(this, Constants.SYNC_TYPE, -1)
        if (oldSyncType == SYNC_TYPE && !syncGroups()) {
            return
        }

        //TODO:: fix when will separate to different accounts
        val mAccountManager = AccountManager.get(applicationContext)
        val availableAccounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)
        val account = availableAccounts[0]

        clearOldAccountContacts(
            contentResolver,
            account
        )

        //if has changes, set sync
        if (syncNothing()) {
            SharedPrefsUtils.putValue(applicationContext, Constants.SYNC_TYPE, Constants.SYNC_TYPE_NO_NEED_SYNC)
            setSync(false)
        } else if (syncAllUsers()) {
            SharedPrefsUtils.putValue(applicationContext, Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS)
            setSync(true)
        } else if (syncGroups()) {
            //compare to see if there are some changes.
            if (oldSyncType == SYNC_TYPE && compareSelectGroups()) {
                Toast.makeText(this, getString(R.string.unchangedSettings), Toast.LENGTH_LONG).show()
                return
            }

            mGroupAdapter!!.saveSelectedGroups()

            SharedPrefsUtils.putValue(applicationContext, Constants.SYNC_TYPE, Constants.SYNC_TYPE_SELECTED_GROUPS)
            setSync(true)
            finish() // TODO:: FIX IT TO CORRECT HANDLE OF COMPLETING SETTINGS
        }
    }

    /**
     * Enable/disable synchronization depending on syncEnabled.
     *
     * @param syncEnabled Flag to enable (if true) / disable (if false) synchronization
     */
    private fun setSync(syncEnabled: Boolean) {
        val mAccountManager = AccountManager.get(applicationContext)
        val availableAccounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)
        val account = availableAccounts[0]
        if (syncEnabled) {
            mAccountManager.setUserData(account, Constants.SYNC_MARKER_KEY, null)
            ContentResolver.cancelSync(account, ContactsContract.AUTHORITY)
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1)
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
            ContentResolver.addPeriodicSync(
                account,
                ContactsContract.AUTHORITY,
                Bundle.EMPTY,
                Constants.SYNC_INTERVAL.toLong()
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
        val newList = mGroupAdapter!!.selectGroups
        //old
        val oldList = SharedPrefsUtils.getArrayList(applicationContext, Constants.SELECTED_GROUPS)
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

    companion object {

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
    }
}
