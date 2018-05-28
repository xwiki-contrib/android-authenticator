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
package org.xwiki.android.authenticator.rest;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.SearchResultContainer;
import org.xwiki.android.authenticator.bean.SerachResults.CustomObjectsSummariesContainer;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.bean.XWikiUserFull;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import rx.functions.Action1;
import rx.functions.Func1;

import static org.xwiki.android.authenticator.AppContext.getApiManager;

/**
 * XWikiHttp
 */
public class XWikiHttp {
    private static final String TAG = "XWikiHttp";
    private static String serverRestPreUrl = null;

    /**
     * before sign up, we should init the cookie, get the form token and the captcha.
     *
     * @return String
     * the form token
     * @throws IOException
     */
    public static HttpResponse signUpInitCookieForm() throws IOException {
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, null);
        String registerUrl = getServerAddress() + "/bin/view/XWiki/Registration";
        if (registerUrl.contains("www.xwiki.org")) {
            registerUrl = getServerAddress() + "/bin/view/XWiki/RealRegistration";
        }
        HttpRequest request = new HttpRequest(registerUrl);
        HttpExecutor httpExecutor = new HttpExecutor();
        HttpResponse response = httpExecutor.performRequest(request);
        return response;
    }

    public static class SyncData {
        //the users which have been modified from the last sync time. mainly used for updating and adding..
        private List<XWikiUserFull> updateUserList;
        //all the users in the server or all the users of the selected groups used for deleting.
        private HashSet<String> allIdSet;

        public SyncData() {
            updateUserList = new ArrayList<>();
            allIdSet = new HashSet<>();
        }

        public List<XWikiUserFull> getUpdateUserList() {
            return updateUserList;
        }

        public HashSet<String> getAllIdSet() {
            return allIdSet;
        }

        @Override
        public String toString() {
            return "SyncData{" +
                "updateUserList=" + updateUserList +
                ", allIdSet=" + allIdSet +
                '}';
        }
    }


    /**
     * getSyncData
     * get SyncData used in SyncAdapter.onPerformSync
     *
     * @param lastSyncTime the last sync time.
     * @param syncType     Constants.SYNC_TYPE_ALL_USERS
     *                     Constants.SYNC_TYPE_SELECTED_GROUPS
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static SyncData getSyncData(String lastSyncTime, int syncType) throws IOException, XmlPullParserException {
        if (syncType == Constants.SYNC_TYPE_ALL_USERS) {
            return getSyncAllUsers(lastSyncTime);
        } else if (syncType == Constants.SYNC_TYPE_SELECTED_GROUPS) {
            List<String> groupIdList = SharedPrefsUtils.getArrayList(AppContext.getInstance().getApplicationContext(), Constants.SELECTED_GROUPS);
            return getSyncGroups(groupIdList, lastSyncTime);
        }
        throw new IOException(TAG + "syncType error, SyncType=" + syncType);
    }

    /**
     * getSyncGroups
     * Constants.SYNC_TYPE_SELECTED_GROUPS
     * get the selected groups' SyncData
     *
     * @param groupIdList  the selected groups' id list.
     * @param lastSyncTime the last sync time.
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static SyncData getSyncGroups(List<String> groupIdList, String lastSyncTime) throws IOException, XmlPullParserException {
        if (groupIdList == null || groupIdList.size() == 0) return null;
        final SyncData syncData = new SyncData();
        final CountDownLatch groupsCountDown = new CountDownLatch(groupIdList.size());
        for (String groupId : groupIdList) {
            String[] split = XWikiUser.splitId(groupId);
            if (split == null) throw new IOException(TAG + ",in getSyncGroups, groupId error");
            getApiManager().getXwikiServicesApi().getGroupMembers(
                split[0],
                split[1],
                split[2]
            ).map(
                // Map<String, String> : keys - usernames, values - spaces
                new Func1<CustomObjectsSummariesContainer<ObjectSummary>, Map<String, String>>() {
                    @Override
                    public Map<String, String> call(CustomObjectsSummariesContainer<ObjectSummary> xWikiUserCustomObjectsSummariesContainer) {
                        Map<String, String> pairs = new HashMap<>();

                        for (ObjectSummary summary : xWikiUserCustomObjectsSummariesContainer.objectSummaries) {
                            try {
                                Map.Entry<String, String> spaceAndName = XWikiUser.spaceAndPage(summary.headline);
                                pairs.put(
                                    spaceAndName.getValue(),
                                    spaceAndName.getKey()
                                );
                            } catch (Exception e) {
                                Log.e(TAG, "Can't transform group member headline", e);
                            }
                        }

                        return pairs;
                    }
                }
            ).subscribe(
                new Action1<Map<String, String>>() {
                    @Override
                    public void call(Map<String, String> pairs) {
                        syncData.getUpdateUserList().addAll(
                            getDetailedInfo(
                                pairs
                            )
                        );
                        groupsCountDown.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        groupsCountDown.countDown();
                    }
                }
            );
        }
        try {
            groupsCountDown.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "Can't await update of user", e);
        }
        return syncData;
    }

    /**
     * @param from Key-value pairs where key - username, value - space
     * @return List of users
     */
    private static List<XWikiUserFull> getDetailedInfo(Map<String, String> from) {
        final List<XWikiUserFull> users = new ArrayList<>();

        final CountDownLatch countDown = new CountDownLatch(from.size());

        for (final String userPage : from.keySet()) {
            getApiManager().getXwikiServicesApi().getFullUserDetails(
                from.get(userPage),
                userPage
            ).subscribe(
                new Action1<XWikiUserFull>() {
                    @Override
                    public void call(XWikiUserFull xWikiUser) {
                        users.add(xWikiUser);
                        countDown.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Can't get user info", throwable);
                        countDown.countDown();
                    }
                }
            );
        }

        try {
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * getSyncAllUsers
     * Constants.SYNC_TYPE_ALL_USERS
     * used for getting the all users from server.
     *
     * @param lastSyncTime the last sync time.
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static SyncData getSyncAllUsers(String lastSyncTime) throws IOException, XmlPullParserException {
        final List<SearchResult> searchList = new ArrayList<>();
        final Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
            getApiManager().getXwikiServicesApi().getAllUsersPreview().subscribe(
                new Action1<SearchResultContainer>() {
                    @Override
                    public void call(SearchResultContainer searchResultContainer) {
                        searchList.addAll(searchResultContainer.searchResults);
                        semaphore.release();
                    }
                }
            );
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final SyncData syncData = new SyncData();
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = null;
        final CountDownLatch countDown = new CountDownLatch(searchList.size());
        for (SearchResult item : searchList) {
            syncData.allIdSet.add(item.id);
            itemDate = StringUtils.iso8601ToDate(item.modified);
            if (itemDate != null && itemDate.before(lastSynDate)) {
                countDown.countDown();
                continue;
            } else {
                String[] splitted = XWikiUser.splitId(item.id);
                String wiki = splitted[0];
                String space = splitted[1];
                String pageName = splitted[2];
                getApiManager().getXwikiServicesApi().getFullUserDetails(
                    wiki,
                    space,
                    pageName
                ).subscribe(
                    new Action1<XWikiUserFull>() {
                        @Override
                        public void call(XWikiUserFull xWikiUser) {
                            syncData.getUpdateUserList().add(xWikiUser);
                            countDown.countDown();
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable e) {
                            Log.e(TAG, "Can't get user", e);
                            countDown.countDown();
                        }
                    }
                );
            }

            // if many users should be synchronized, the task will not be stop
            // even though you close the sync in settings or selecting the "don't sync" option.
            // we should stop the task by checking the sync type each time.
            int syncType = SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.SYNC_TYPE, -1);
            if (syncType != Constants.SYNC_TYPE_ALL_USERS) {
                throw new IOException("the sync type has been changed");
            } else {
                countDown.countDown();
            }
        }
        try {
            countDown.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "Can't perform `getSyncAllUsers`", e);
        }
        return syncData;
    }

    /**
     * getServerRestUrl
     * get serverRestPreUrl from preference.
     * http://www.xwiki.org/xwiki + "/rest"
     *
     * @return String
     * url
     */
    public static String getServerRestUrl() {
        if (serverRestPreUrl == null) {
            serverRestPreUrl = getServerAddress() + "/rest";
        }
        return serverRestPreUrl;
    }

    public static String getServerAddress() {
        return SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.SERVER_ADDRESS, null);
    }

    public static void setRestUrlNULL() {
        serverRestPreUrl = null;
    }
}
