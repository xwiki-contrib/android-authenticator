package org.xwiki.android.authenticator.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.SharedPrefsUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lf on 2016/5/13.
 */
public class SettingViewFlipper {
    protected AuthenticatorActivity mActivity;
    protected Context mContext;
    private View mContentRootView;

    ListView mListView = null;
    GroupListAdapter mAdapter;
    private List<XWikiGroup> groupList;
    private List<XWikiGroup> selectList;
    private RadioGroup radioGroup;
    private Button cancelButton;
    private Button okButton;

    public SettingViewFlipper(AuthenticatorActivity activity, View contentRootView){
        mActivity = activity;
        mContext = (Context) mActivity;
        mContentRootView = contentRootView;
        initData();
    }

    public View findViewById(int id) {
        return mContentRootView.findViewById(id);
    }

    public void initData(){
        mListView = (ListView)findViewById(R.id.list_view);
        groupList = new ArrayList<>();
        mAdapter = new GroupListAdapter(mContext, groupList);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        cancelButton = (Button) findViewById(R.id.settings_cancel);
        okButton = (Button) findViewById(R.id.settings_ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsOK();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.radio_sync_type);
        int syncType = SharedPrefsUtil.getValue(mContext, "SyncType", Constants.SYNC_TYPE_ALL_USERS);
        if(syncType == 1) {
            radioGroup.check(R.id.radio_all_users);
            mListView.setVisibility(View.GONE);
        }else{
            radioGroup.check(R.id.radio_selected_groups);
            mListView.setVisibility(View.VISIBLE);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.radio_all_users) {
                    mListView.setVisibility(View.GONE);
                } else if(checkedId == R.id.radio_selected_groups) {
                    mListView.setVisibility(View.VISIBLE);
                }
            }
        });

        new AsyncTask<String, String, List<XWikiGroup>>() {
            @Override
            protected List<XWikiGroup> doInBackground(String... params) {
                try {
                    List<XWikiGroup> groups = XWikiHttp.getGroupList(100);
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
                if(groups != null && groups.size()>=0){
                    Log.i("Group", groups.toString());
                    groupList.addAll(groups);
                    mAdapter.refresh(groupList);
                }
            }
        }.execute();
    }


    void settingsOK(){
        if(radioGroup.getCheckedRadioButtonId() == R.id.radio_all_users){
            //TODO AccountManager.setUserData(MAKER, NULL);
            SharedPrefsUtil.putValue(mContext.getApplicationContext(), "SyncType", Constants.SYNC_TYPE_ALL_USERS);
        }else {
            List<XWikiGroup> list = mAdapter.getSelectGroups();
            Toast.makeText(mContext, mAdapter.getSelectGroups().toString() ,Toast.LENGTH_SHORT).show();
            if(list != null && list.size()>0){
                List<String> groupIdList = new ArrayList<>();
                for(XWikiGroup iGroup: list){
                    groupIdList.add(iGroup.id);
                }
                SharedPrefsUtil.putArrayList(mContext.getApplicationContext(), "SelectGroups", groupIdList);
            }else{
                SharedPrefsUtil.putArrayList(mContext.getApplicationContext(), "SelectGroups", new ArrayList<String>());
            }
            SharedPrefsUtil.putValue(mContext.getApplicationContext(), "SyncType", Constants.SYNC_TYPE_SELECTED_GROUPS);
        }
        resetSyncMaker();
        mActivity.finish();
    }

    private void resetSyncMaker(){
        AccountManager mAccountManager = AccountManager.get(mContext.getApplicationContext());
        Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Account account = availableAccounts[0];
        mAccountManager.setUserData(account, Constants.SYNC_MARKER_KEY, null);
        ContentResolver.cancelSync(account, ContactsContract.AUTHORITY);
        ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
    }


}
