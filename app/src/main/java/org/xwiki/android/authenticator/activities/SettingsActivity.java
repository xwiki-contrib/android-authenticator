package org.xwiki.android.authenticator.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.SharedPrefsUtil;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A Select groups screen..
 */
public class SettingsActivity extends AppCompatActivity {
    ListView mListView = null;
    GroupListAdapter mAdapter;
    private List<XWikiGroup> groupList;
    private List<XWikiGroup> selectList;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_select_groups);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));



        // init view
        mListView = (ListView)findViewById(R.id.list_view);
        groupList = new ArrayList<>();
        mAdapter = new GroupListAdapter(this, groupList);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        radioGroup = (RadioGroup) findViewById(R.id.radio_sync_type);
        int syncType = SharedPrefsUtil.getValue(this, "SyncType", Constants.SYNC_TYPE_ALL_USERS);
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

        initDate();
    }

    private void initDate(){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId()==R.id.action_save){
            if(radioGroup.getCheckedRadioButtonId() == R.id.radio_all_users){
                //TODO AccountManager.setUserData(MAKER, NULL);
                SharedPrefsUtil.putValue(getApplicationContext(), "SyncType", Constants.SYNC_TYPE_ALL_USERS);
            }else {
                List<XWikiGroup> list = mAdapter.getSelectGroups();
                Toast.makeText(SettingsActivity.this, mAdapter.getSelectGroups().toString() ,Toast.LENGTH_SHORT).show();
                if(list != null && list.size()>0){
                    List<String> groupIdList = new ArrayList<>();
                    for(XWikiGroup iGroup: list){
                        groupIdList.add(iGroup.id);
                    }
                    SharedPrefsUtil.putArrayList(getApplicationContext(), "SelectGroups", groupIdList);
                }else{
                    SharedPrefsUtil.putArrayList(getApplicationContext(), "SelectGroups", new ArrayList<String>());
                }
                SharedPrefsUtil.putValue(getApplicationContext(), "SyncType", Constants.SYNC_TYPE_SELECTED_GROUPS);
            }
            resetSyncMaker();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetSyncMaker(){
        AccountManager mAccountManager = AccountManager.get(getApplicationContext());
        Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Account account = availableAccounts[0];
        mAccountManager.setUserData(account, Constants.SYNC_MARKER_KEY, null);
        ContentResolver.cancelSync(account, ContactsContract.AUTHORITY);
        ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        //Bundle params = new Bundle();
        //params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
        //params.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false);
        //params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        //ContentResolver.requestSync(account, ContactsContract.AUTHORITY, params);
    }

}

