package org.xwiki.android.sync.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.activities.base.BaseActivity;
import org.xwiki.android.sync.bean.ObjectSummary;
import org.xwiki.android.sync.bean.SerachResults.CustomObjectsSummariesContainer;
import org.xwiki.android.sync.bean.SerachResults.CustomSearchResultContainer;
import org.xwiki.android.sync.bean.XWikiGroup;
import org.xwiki.android.sync.utils.SharedPrefsUtils;
import org.xwiki.android.sync.utils.SystemTools;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.xwiki.android.sync.AppContext.getApiManager;
import static org.xwiki.android.sync.contactdb.ContactOperationsKt.clearOldAccountContacts;

public class SyncSettingsActivity extends BaseActivity {

    /**
     * Tag which will be used for logging.
     */
    private static final String TAG = SyncSettingsActivity.class.getSimpleName();

    /**
     * {@link View} for presenting items.
     */
    private ListView mListView = null;

    /**
     * Adapter for groups
     */
    private GroupListAdapter mGroupAdapter;

    /**
     * Adapter for users.
     */
    private UserListAdapter mUsersAdapter;

    /**
     * List of received groups.
     */
    private List<XWikiGroup> groups;

    /**
     * List of received all users.
     */
    private List<ObjectSummary> allUsers;

    /**
     * Currently chosen sync type.
     */
    private int SYNC_TYPE = Constants.SYNC_TYPE_NO_NEED_SYNC;

    /**
     * Flag of currently loading groups.
     */
    private volatile Boolean groupsAreLoading = false;

    /**
     * Flag of currently loading all users.
     */
    private volatile Boolean allUsersAreLoading = false;

    /**
     * Init all views and other activity objects
     *
     * @param savedInstanceState
     *
     * @since 1.0
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_settings);

        Button versionCheckButton = findViewById(R.id.version_check);
        versionCheckButton.setText(
            String.format(
                getString(R.string.versionTemplate),
                SystemTools.getAppVersionName(this)
            )
        );
        versionCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppMarket(v.getContext());
            }
        });

        mListView = findViewById(R.id.list_view);
        mListView.setEmptyView(
            findViewById(R.id.syncTypeGetErrorContainer)
        );
        groups = new ArrayList<>();
        allUsers = new ArrayList<>();
        mGroupAdapter = new GroupListAdapter(this, groups);
        mUsersAdapter = new UserListAdapter(this, allUsers);
        initData(null);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        AppCompatSpinner selectSyncSpinner = getSelectSyncSpinner();
        selectSyncSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SYNC_TYPE = position;
                updateListView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SYNC_TYPE = SharedPrefsUtils.getValue(this, Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS);
        selectSyncSpinner.setSelection(SYNC_TYPE);
    }

    /**
     * @return Spinner for sync type
     *
     * @since 0.4.2
     */
    private AppCompatSpinner getSelectSyncSpinner() {
        return findViewById(R.id.select_spinner);
    }

    /**
     * @return Progress bar view
     *
     * @since 0.4.2
     */
    private ProgressBar getProgressBar() {
        return findViewById(R.id.list_viewProgressBar);
    }

    /**
     * @return Container of {@link #mListView}
     *
     * @since 0.4.2
     */
    private View getListViewContainer() {
        return findViewById(R.id.settingsSyncListViewContainer);
    }

    /**
     * Show progress bar if need or hide otherwise.
     *
     * @since 0.4.2
     */
    private void refreshProgressBar() {
        final Boolean progressBarVisible = (syncGroups() && groupsAreLoading)
            || (syncAllUsers() && allUsersAreLoading);
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    if (progressBarVisible) {
                        getProgressBar().setVisibility(View.VISIBLE);
                        getListViewContainer().setVisibility(View.GONE);
                    } else {
                        getProgressBar().setVisibility(View.GONE);
                        getListViewContainer().setVisibility(View.VISIBLE);
                    }
                }
            }
        );
    }

    /**
     * Load data to groups and all users lists.
     *
     * @since 0.4
     */
    public void initData(View v) {
        if (groups.isEmpty()) {
            groupsAreLoading = true;
            getApiManager().getXwikiServicesApi().availableGroups(
                Constants.LIMIT_MAX_SYNC_USERS
            )
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    new Action1<CustomSearchResultContainer<XWikiGroup>>() {
                        @Override
                        public void call(CustomSearchResultContainer<XWikiGroup> xWikiGroupCustomSearchResultContainer) {
                            groupsAreLoading = false;
                            List<XWikiGroup> searchResults = xWikiGroupCustomSearchResultContainer.searchResults;
                            if (searchResults != null) {
                                groups.clear();
                                groups.addAll(searchResults);
                                updateListView();
                            }
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            groupsAreLoading = false;
                            runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                            SyncSettingsActivity.this,
                                            R.string.cantGetGroups,
                                            Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                            );
                            refreshProgressBar();
                        }
                    }
                );
        }
        if (allUsers.isEmpty()) {
            allUsersAreLoading = true;
            getApiManager().getXwikiServicesApi().getAllUsersPreview()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    new Action1<CustomObjectsSummariesContainer<ObjectSummary>>() {
                        @Override
                        public void call(CustomObjectsSummariesContainer<ObjectSummary> summaries) {
                            allUsersAreLoading = false;
                            allUsers.clear();
                            allUsers.addAll(summaries.objectSummaries);
                            updateListView();
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            allUsersAreLoading = false;
                            runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                            SyncSettingsActivity.this,
                                            R.string.cantGetAllUsers,
                                            Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                            );
                            refreshProgressBar();
                        }
                    }
                );
        }
        if (allUsersAreLoading || groupsAreLoading) {
            refreshProgressBar();
        }
    }

    /**
     * @return true if currently selected to sync groups or false otherwise
     */
    private Boolean syncGroups() {
        return SYNC_TYPE == Constants.SYNC_TYPE_SELECTED_GROUPS;
    }

    /**
     * @return true if currently selected to sync all users or false otherwise
     */
    private Boolean syncAllUsers() {
        return SYNC_TYPE == Constants.SYNC_TYPE_ALL_USERS;
    }

    /**
     * @return true if currently selected to sync not or false otherwise
     */
    private Boolean syncNothing() {
        return SYNC_TYPE == Constants.SYNC_TYPE_NO_NEED_SYNC;
    }

    /**
     * Update list view and hide/show view from {@link #getListViewContainer()}
     */
    private void updateListView() {
        if(syncNothing()){
            getListViewContainer().setVisibility(View.GONE);
            getProgressBar().setVisibility(View.GONE);
        } else {
            getListViewContainer().setVisibility(View.VISIBLE);
            BaseAdapter adapter;
            if(syncGroups()){
                adapter = mGroupAdapter;
                mGroupAdapter.refresh(groups);
            } else {
                adapter = mUsersAdapter;
                mUsersAdapter.refresh(allUsers);
            }
            if (adapter != mListView.getAdapter()) {
                mListView.setAdapter(adapter);
            }
            refreshProgressBar();
        }
    }

    /**
     * Save settings of synchronization.
     */
    public void syncSettingComplete(View v) {
        //check changes. if no change, directly return
        int oldSyncType = SharedPrefsUtils.getValue(this, Constants.SYNC_TYPE, -1);
        if(oldSyncType == SYNC_TYPE && !syncGroups()){
            return;
        }

        //TODO:: fix when will separate to different accounts
        AccountManager mAccountManager = AccountManager.get(getApplicationContext());
        Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Account account = availableAccounts[0];

        clearOldAccountContacts(
            getContentResolver(),
            account
        );

        //if has changes, set sync
        if(syncNothing()){
            SharedPrefsUtils.putValue(getApplicationContext(), Constants.SYNC_TYPE, Constants.SYNC_TYPE_NO_NEED_SYNC);
            setSync(false);
        } else if (syncAllUsers()) {
            SharedPrefsUtils.putValue(getApplicationContext(), Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS);
            setSync(true);
        } else if(syncGroups()){
            //compare to see if there are some changes.
            if(oldSyncType == SYNC_TYPE && compareSelectGroups()){
                return;
            }

            mGroupAdapter.saveSelectedGroups();

            SharedPrefsUtils.putValue(getApplicationContext(), Constants.SYNC_TYPE, Constants.SYNC_TYPE_SELECTED_GROUPS);
            setSync(true);
        }
    }

    /**
     * Enable/disable synchronization depending on syncEnabled.
     *
     * @param syncEnabled Flag to enable (if true) / disable (if false) synchronization
     */
    private void setSync(boolean syncEnabled) {
        AccountManager mAccountManager = AccountManager.get(getApplicationContext());
        Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Account account = availableAccounts[0];
        if (syncEnabled) {
            mAccountManager.setUserData(account, Constants.SYNC_MARKER_KEY, null);
            ContentResolver.cancelSync(account, ContactsContract.AUTHORITY);
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
            ContentResolver.addPeriodicSync(
                account,
                ContactsContract.AUTHORITY,
                Bundle.EMPTY,
                Constants.SYNC_INTERVAL);
            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY);
        } else {
            ContentResolver.cancelSync(account, ContactsContract.AUTHORITY);
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 0);
        }
    }

    /**
     * @return true if old list equal to new list of groups
     */
    private boolean compareSelectGroups(){
        //new
        List<XWikiGroup> newList = mGroupAdapter.getSelectGroups();
        //old
        List<String> oldList = SharedPrefsUtils.getArrayList(getApplicationContext(), Constants.SELECTED_GROUPS);
        if(newList == null && oldList == null){
            return true;
        }else if(newList != null && oldList != null){
            if(newList.size() != oldList.size()){
                return false;
            }else{
                for(XWikiGroup item : newList){
                    if(!oldList.contains(item.id)){
                        return false;
                    }
                }
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * Open market with application page.
     *
     * @param context Context to know where from to open market
     */
    private static void openAppMarket(Context context) {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
        boolean marketFound = false;
        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager().queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {
                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                    otherAppActivity.applicationInfo.packageName,
                    otherAppActivity.name
                );
                rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;
            }
        }
        // if GooglePlay not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+context.getPackageName()));
            context.startActivity(webIntent);
        }
    }
}
