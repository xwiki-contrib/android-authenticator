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
package org.xwiki.android.sync.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import org.xwiki.android.sync.bean.XWikiUserFull;
import org.xwiki.android.sync.contactdb.*;
import org.xwiki.android.sync.rest.XWikiHttp;
import org.xwiki.android.sync.utils.StringUtils;
import rx.Observable;
import rx.Observer;
import java.util.Date;
import static org.xwiki.android.sync.AppContextKt.*;
import static org.xwiki.android.sync.ConstantsKt.SYNC_MARKER_KEY;
import static org.xwiki.android.sync.ConstantsKt.SYNC_TYPE_NO_NEED_SYNC;
import static org.xwiki.android.sync.contactdb.ContactOperationsKt.setAccountContactsVisibility;

/**
 * Adapter which will be used for synchronization.
 *
 * @version $Id$
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * Tag for logging.
     */
    private static final String TAG = "SyncAdapter";

    /**
     * Account manager to manage synchronization.
     */
    private final AccountManager mAccountManager;

    /**
     * Context for all operations.
     */
    private final Context mContext;

    /**
     * @param context will be set to {@link #mContext}
     * @param autoInitialize auto initialization sync
     * @param allowParallelSyncs flag about paralleling of sync
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    /**
     * @param context will be set to {@link #mContext}
     * @param autoInitialize auto initialization sync
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    /**
     * Perform all sync process.
     *
     * @param account the account that should be synced
     * @param extras SyncAdapter-specific parameters
     * @param authority the authority of this sync request
     * @param provider a ContentProviderClient that points to the ContentProvider for this
     *   authority
     * @param syncResult SyncAdapter-specific parameters
     */
    @Override
    public void onPerformSync(
        final Account account,
        Bundle extras,
        String authority,
        ContentProviderClient provider,
        final SyncResult syncResult)
    {
        setAccountContactsVisibility(
            getContext().getContentResolver(),
            account,
            false
        );
        Log.i(TAG, "onPerformSync start");
        UserDao userDao = AppDatabase.Companion.getInstance(getContext()).userDao();
        AppRepository appRepository = new AppRepository(userDao, null, null);
        User user = appRepository.getAccountByName(account.name);
        int syncType = user.getSyncType();
        getAccountServerUrl(user.getAccountName());

        Log.i(TAG, "syncType=" + syncType);
        if (syncType == SYNC_TYPE_NO_NEED_SYNC) return;
        // get last sync date. return new Date(0) if first onPerformSync
        String lastSyncMarker = getServerSyncMarker(account);
        Log.d(TAG, lastSyncMarker);

        // Get XWiki SyncData from XWiki server , which should be added, updated or deleted after lastSyncMarker.
        final Observable<XWikiUserFull> observable = XWikiHttp.getSyncData(
            syncType,
            account.name,
            user.getSelectedGroupsList()
        );

        // Update the local contacts database with the last modified changes. updateContact()
        ContactManager.updateContacts(mContext, account.name, observable);

        final Object[] sync = new Object[]{null};

        observable.subscribe(
            new Observer<XWikiUserFull>() {
                @Override
                public void onCompleted() {
                    // Save off the new sync date. On our next sync, we only want to receive
                    // contacts that have changed since this sync...
                    setServerSyncMarker(account, StringUtils.dateToIso8601String(new Date()));
                    synchronized (sync) {
                        sync[0] = new Object();
                        sync.notifyAll();
                    }
                    setAccountContactsVisibility(
                        getContext().getContentResolver(),
                        account,
                        true
                    );
                }

                @Override
                public void onError(Throwable e) {
                    syncResult.stats.numIoExceptions++;
                    synchronized (sync) {
                        sync[0] = new Object();
                        sync.notifyAll();
                    }
                    setAccountContactsVisibility(
                        getContext().getContentResolver(),
                        account,
                        true
                    );
                }

                @Override
                public void onNext(XWikiUserFull xWikiUserFull) {
                    syncResult.stats.numEntries++;
                }
            }
        );

        synchronized (observable) {
            observable.notifyAll();
        }
        synchronized (sync) {
            while (sync[0] == null) {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Can't await end of sync", e);
                }
            }
        }
    }

    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     *
     * @param account the account we're syncing
     * @return the change high-water-mark  Iso8601
     */
    private String getServerSyncMarker(Account account) {
        String lastSyncIso = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        //if empty, just return new Date(0) so that we can get all users from server.
        if (TextUtils.isEmpty(lastSyncIso)) {
            return StringUtils.dateToIso8601String(new Date(0));
        }
        return lastSyncIso;
    }

    /**
     * Save off the high-water-mark we receive back from the server.
     *
     * @param account     The account we're syncing
     * @param lastSyncIso The high-water-mark we want to save.
     */
    private void setServerSyncMarker(Account account, String lastSyncIso) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, lastSyncIso);
    }
}

