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
package org.xwiki.android.authenticator.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SettingSyncViewFlipper
 */
public class SettingSyncViewFlipper extends BaseViewFlipper {
    private static final String TAG = "SettingSyncViewFlipper";

    ListView mListView = null;
    GroupListAdapter mAdapter;
    private List<XWikiGroup> groupList;
    private RadioGroup radioGroup;

    public SettingSyncViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
        initView();
        initData();
    }

    @Override
    public void doNext() {
        syncSettingComplete();
        mActivity.finish();
    }

    @Override
    public void doPrevious() {
        SharedPrefsUtils.removeKeyValue(mContext, Constants.SYNC_TYPE);
        resetSync(false);
        mActivity.finish();
    }

    private void initView(){
        mListView = (ListView) findViewById(R.id.list_view);
        groupList = new ArrayList<>();
        mAdapter = new GroupListAdapter(mContext, groupList);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        radioGroup = (RadioGroup) findViewById(R.id.radio_sync_type);
        int syncType = SharedPrefsUtils.getValue(mContext, Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS);
        if (syncType == Constants.SYNC_TYPE_ALL_USERS) {
            radioGroup.check(R.id.radio_all_users);
            mListView.setVisibility(View.GONE);
        } else {
            radioGroup.check(R.id.radio_selected_groups);
            mListView.setVisibility(View.VISIBLE);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radio_all_users) {
                    mListView.setVisibility(View.GONE);
                } else if (checkedId == R.id.radio_selected_groups) {
                    mListView.setVisibility(View.VISIBLE);
                }
            }
        });

        mActivity.refreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
    }

    private void initData() {
        //mActivity.swipeRefreshLayout.setRefreshing(true);
        refreshImageView(mActivity.refreshImageView);
        AsyncTask getGroupsTask = new AsyncTask<Void, Void, List<XWikiGroup>>() {
            @Override
            protected List<XWikiGroup> doInBackground(Void... params) {
                try {
                    List<XWikiGroup> groups = XWikiHttp.getGroupList(Constants.LIMIT_MAX_SYNC_USERS);
                    return groups;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<XWikiGroup> groups) {
                hideRefreshAnimation(mActivity.refreshImageView);
                if (groups != null && groups.size() >= 0) {
                    Log.i(TAG, groups.toString());
                    groupList.clear();
                    groupList.addAll(groups);
                    mAdapter.refresh(groupList);
                }
            }


        };
        mActivity.putAsyncTask(getGroupsTask);
    }


    void syncSettingComplete() {
        if (radioGroup.getCheckedRadioButtonId() == R.id.radio_all_users) {
            SharedPrefsUtils.putValue(mContext.getApplicationContext(), Constants.SYNC_TYPE, Constants.SYNC_TYPE_ALL_USERS);
        } else {
            List<XWikiGroup> list = mAdapter.getSelectGroups();
            //Toast.makeText(mContext, mAdapter.getSelectGroups().toString(), Toast.LENGTH_SHORT).show();
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
        }
        resetSync(true);
        mActivity.finish();
    }

    private void resetSync(boolean flag) {
        AccountManager mAccountManager = AccountManager.get(mContext.getApplicationContext());
        Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Account account = availableAccounts[0];
        if (flag) {
            //reset sync
            mAccountManager.setUserData(account, Constants.SYNC_MARKER_KEY, null);
            ContentResolver.cancelSync(account, ContactsContract.AUTHORITY);
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        } else {
            //don't sync
            ContentResolver.cancelSync(account, ContactsContract.AUTHORITY);
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 0);
        }
    }

    public void onRefresh(){
        initData();
    }


    /**
     * animation refresh
     */
    private Animation animation;
    public void refreshImageView(View v) {
        hideRefreshAnimation(v);
        //refresh anim
        animation = AnimationUtils.loadAnimation(mContext, R.anim.refresh);
        //Defines what this animation should do when it reaches the end
        animation.setRepeatMode(Animation.RESTART);
        //repeat times
        animation.setRepeatCount(Animation.INFINITE);
        //ImageView startt anim
        v.startAnimation(animation);
    }
    public void hideRefreshAnimation(View v) {
        if (animation != null) {
            animation.cancel();
            v.clearAnimation();
            v.setAnimation(null);
//        	v.setImageResource(R.drawable.refresh);
        }
    }

}
