/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
import org.xwiki.android.sync.auth.AuthenticatorActivity;
import org.xwiki.android.sync.bean.SearchResult;
import org.xwiki.android.sync.bean.SearchResultContainer;
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

/**
 * Flipper which contains setting of synchronization.
 *
 * @version $Id$
 */
public class SettingSyncViewFlipper extends BaseViewFlipper {

    /**
     * Tag which will be used for logging.
     */
    private static final String TAG = "SettingSyncViewFlipper";

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
    private List<SearchResult> allUsers;

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
     * Standard constructor.
     *
     * @param activity Current activity
     * @param contentRootView Root view for flipper
     */
    public SettingSyncViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
        initView();
    }

    /**
     * Calling when user push "Complete".
     */
    @Override
    public void doNext() {
        syncSettingComplete();
    }

    /**
     * Calling when user push back.
     */
    @Override
    public void doPrevious() {
        mActivity.finish();
    }

    /**
     * Init current view.
     */
    private void initView(){
        Button versionCheckButton = findViewById(R.id.version_check);
        versionCheckButton.setText(
            String.format(
                mContext.getString(R.string.versionTemplate),
                SystemTools.getAppVersionName(mContext)
            )
        );
        versionCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppMarket(mContext);
            }
        });

        findViewById(R.id.settingsSyncRefreshCurrentTypeListButton).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initData();
                }
            }
        );

        mListView = findViewById(R.id.list_view);
        mListView.setEmptyView(
            findViewById(R.id.syncTypeGetErrorContainer)
        );
        groups = new ArrayList<>();
        allUsers = new ArrayList<>();
        mGroupAdapter = new GroupListAdapter(mContext, groups);
        mUsersAdapter = new UserListAdapter(mContext, allUsers);
        initData();
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

        SYNC_TYPE = SharedPrefsUtils.getValue(mContext, Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS);
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
        mActivity.runOnUiThread(
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
    private void initData() {
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
                            mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                            mActivity,
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
                    new Action1<SearchResultContainer>() {
                        @Override
                        public void call(SearchResultContainer searchResultContainer) {
                            allUsersAreLoading = false;
                            allUsers.clear();
                            allUsers.addAll(searchResultContainer.searchResults);
                            updateListView();
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            allUsersAreLoading = false;
                            mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                            mActivity,
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
     * Will be called when not enough given permissions.
     */
    public void noPermissions(){
        Toast.makeText(mContext, R.string.askToGrantPermissions, Toast.LENGTH_SHORT).show();
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
            adapter.notifyDataSetChanged();
        }
        refreshProgressBar();
    }

    /**
     * Save settings of synchronization.
     */
    public void syncSettingComplete() {
        //check changes. if no change, directly return
        int oldSyncType = SharedPrefsUtils.getValue(mContext, Constants.SYNC_TYPE, -1);
        if(oldSyncType == SYNC_TYPE && !syncGroups()){
            return;
        }
        //if has changes, set sync
        if(syncNothing()){
            SharedPrefsUtils.putValue(mContext.getApplicationContext(), Constants.SYNC_TYPE, Constants.SYNC_TYPE_NO_NEED_SYNC);
            setSync(false);
        } else if (syncAllUsers()) {
            SharedPrefsUtils.putValue(mContext.getApplicationContext(), Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS);
            setSync(true);
        } else if(syncGroups()){
            //compare to see if there are some changes.
            if(oldSyncType == SYNC_TYPE && compareSelectGroups()){
                return;
            }
            List<XWikiGroup> list = mGroupAdapter.getSelectGroups();
            if (list != null && list.size() > 0) {
                List<String> groupIdList = new ArrayList<>();
                for (XWikiGroup iGroup : list) {
                    groupIdList.add(iGroup.id);
                }
                SharedPrefsUtils.putArrayList(mContext.getApplicationContext(), Constants.SELECTED_GROUPS, groupIdList);
            } else {
                SharedPrefsUtils.putArrayList(mContext.getApplicationContext(), Constants.SELECTED_GROUPS, new ArrayList<String>());
            }
            SharedPrefsUtils.putValue(mContext.getApplicationContext(), Constants.SYNC_TYPE, Constants.SYNC_TYPE_SELECTED_GROUPS);
            setSync(true);
        }
        mActivity.finish();
    }

    /**
     * Enable/disable synchronization depending on syncEnabled.
     *
     * @param syncEnabled Flag to enable (if true) / disable (if false) synchronization
     */
    private void setSync(boolean syncEnabled) {
        AccountManager mAccountManager = AccountManager.get(mContext.getApplicationContext());
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
     * @return true if old list is not equal to new list of groups
     */
    private boolean compareSelectGroups(){
        //new
        List<XWikiGroup> newList = mGroupAdapter.getSelectGroups();
        //old
        List<String> oldList = SharedPrefsUtils.getArrayList(mContext.getApplicationContext(), Constants.SELECTED_GROUPS);
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
