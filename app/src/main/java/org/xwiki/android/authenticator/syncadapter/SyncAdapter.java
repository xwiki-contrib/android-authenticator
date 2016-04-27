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
package org.xwiki.android.authenticator.syncadapter;

import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.contactdb.ContactManager;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.Loger;
import org.xwiki.android.authenticator.utils.StringUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    private static final String SYNC_MARKER_KEY = "org.xwiki.android.sync.marker";
    private static final boolean NOTIFY_AUTH_FAILURE = true;

    private final AccountManager mAccountManager;

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
        Loger.debug("SyncAdapter created.");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {

        Loger.debug("SyncAdapter onPerformSync start");

        try {
            // see if we already have a sync-state attached to this account. By handing
            // This value to the server, we can just get the contacts that have
            // been updated on the server-side since our last sync-up
            String lastSyncMarker = getServerSyncMarker(account);

            // By default, contacts from a 3rd party provider are hidden in the contacts
            // list. So let's set the flag that causes them to be visible, so that users
            // can actually see these contacts.
            //"1980-09-24T19:45:31+02:00"
            if (lastSyncMarker.equals(StringUtils.dateToIso8601String(new Date(0))) ) {
                ContactManager.setAccountContactsVisibility(getContext(), account, true);
            }

            // Use the account manager to request the AuthToken we'll need
            // to talk to our sample server.  If we don't have an AuthToken
            // yet, this could involve a round-trip to the server to request
            // and AuthToken.

            //final String authtoken = mAccountManager.blockingGetAuthToken(account,
            //        AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, NOTIFY_AUTH_FAILURE);

            // Make sure that the XWiki group exists
            final long groupId = ContactManager.ensureXWikiGroupExists(mContext, account);

            // Get XWiki users
            List<XWikiUser>  updatedContacts = new XWikiHttp().getUserList("xwiki", 10, lastSyncMarker);
            Loger.debug(updatedContacts.toString());
            // Update the local contacts database with the changes. updateContacts()
            // returns a syncState value that indicates the high-water-mark for
            // the changes we received.
            Log.d(TAG, "Calling contactManager's sync contacts");

            ContactManager.updateContacts(mContext, account.name, updatedContacts, groupId);


            // Save off the new sync marker. On our next sync, we only want to receive
            // contacts that have changed since this sync...
            setServerSyncMarker(account, StringUtils.dateToIso8601String(new Date()));

        } catch (final IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        }
//        catch (final AuthenticatorException e) {
//            Log.e(TAG, "AuthenticatorException", e);
//            syncResult.stats.numParseExceptions++;
//        } catch (final OperationCanceledException e) {
//            Log.e(TAG, "OperationCanceledExcetpion", e);
//        }
//        catch (final JSONException e) {
//            Log.e(TAG, "JSONException", e);
//            syncResult.stats.numParseExceptions++;
//        }catch (final AuthenticationException e) {
//            Log.e(TAG, "AuthenticationException", e);
//            syncResult.stats.numAuthExceptions++;
//        }

    }

    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     * @param account the account we're syncing
     * @return the change high-water-mark  Iso8601
     */
    private String getServerSyncMarker(Account account) {
        String lastSyncIso = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        if (TextUtils.isEmpty(lastSyncIso)) {
            return StringUtils.dateToIso8601String(new Date(0));
        }
        return lastSyncIso;
    }

    /**
     * Save off the high-water-mark we receive back from the server.
     * @param account The account we're syncing
     * @param lastSyncIso The high-water-mark we want to save.
     */
    private void setServerSyncMarker(Account account, String lastSyncIso) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, lastSyncIso);
    }
}

