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

import android.support.annotation.Nullable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.RegisterForm;
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
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static org.xwiki.android.authenticator.AppContext.getApiManager;

/**
 * XWikiHttp
 */
public class XWikiHttp {
    private static final String TAG = "XWikiHttp";

    public static Observable<String> login(
        String username,
        String password
    ) {
        final PublishSubject<String> authTokenSubject = PublishSubject.create();
        getApiManager().getXwikiServicesApi().login(
            Credentials.basic(username, password)
        ).subscribeOn(
            Schedulers.newThread()
        ).subscribe(
            new Action1<Response<ResponseBody>>() {
                @Override
                public void call(Response<ResponseBody> responseBodyResponse) {
                    if (responseBodyResponse.code() >= 200 && responseBodyResponse.code() <= 209) {
                        authTokenSubject.onNext(responseBodyResponse.headers().get("Set-Cookie"));
                    } else {
                        authTokenSubject.onNext(null);
                    }
                    authTokenSubject.onCompleted();
                }
            },
            new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    authTokenSubject.onError(throwable);
                }
            }
        );
        return authTokenSubject;
    }

    /**
     * getSyncData
     * get SyncData used in SyncAdapter.onPerformSync
     *
     * @param syncType     Constants.SYNC_TYPE_ALL_USERS
     *                     Constants.SYNC_TYPE_SELECTED_GROUPS
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static Observable<XWikiUserFull> getSyncData(final int syncType) {
        final PublishSubject<XWikiUserFull> subject = PublishSubject.create();
        final Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            synchronized (subject) {
                                try {
                                    semaphore.release();
                                    subject.wait();
                                } catch (InterruptedException e) {
                                    subject.onError(e);
                                    return;
                                }
                            }
                            if (syncType == Constants.SYNC_TYPE_ALL_USERS) {
                                getSyncAllUsers(subject);
                            } else if (syncType == Constants.SYNC_TYPE_SELECTED_GROUPS) {
                                List<String> groupIdList = SharedPrefsUtils.getArrayList(AppContext.getInstance().getApplicationContext(), Constants.SELECTED_GROUPS);
                                getSyncGroups(groupIdList, subject);
                            } else {
                                throw new IOException(TAG + "syncType error, SyncType=" + syncType);
                            }
                        } catch (IOException e) {
                            subject.onError(e);
                        }
                    }
                }
            ).start();
            semaphore.acquire();
        } catch (InterruptedException e) {
            return subject;
        }
        return subject;
    }

    /**
     * getSyncGroups
     * Constants.SYNC_TYPE_SELECTED_GROUPS
     * get the selected groups' SyncData
     *
     * @param groupIdList  the selected groups' id list.
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static void getSyncGroups(
        List<String> groupIdList,
        final PublishSubject<XWikiUserFull> subject
    ) throws IOException {
        final CountDownLatch groupsCountDown = new CountDownLatch(groupIdList.size());
        for (String groupId : groupIdList) {
            if (subject.getThrowable() != null) {
                return;
            }
            String[] split = XWikiUser.splitId(groupId);
            if (split == null) {
                IOException exception = new IOException(TAG + ",in getSyncGroups, groupId error");
                subject.onError(exception);
                throw exception;
            }
            getApiManager().getXwikiServicesApi().getGroupMembers(
                split[0],
                split[1],
                split[2]
            ).subscribe(
                new Action1<CustomObjectsSummariesContainer<ObjectSummary>>() {
                    @Override
                    public void call(CustomObjectsSummariesContainer<ObjectSummary> summaries) {
                        try {
                            getDetailedInfo(
                                summaries.objectSummaries,
                                subject
                            );
                        } catch (IOException e) {
                            Log.e(TAG, "Can't get users info", e);
                            subject.onError(e);
                        }
                        groupsCountDown.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        subject.onError(throwable);
                        groupsCountDown.countDown();
                    }
                }
            );
        }
    }

    /**
     * @param from Key-value pairs where key - username, value - space
     * @return List of users
     */
    private static void getDetailedInfo(
        List<ObjectSummary> from,
        final PublishSubject<XWikiUserFull> subject
    ) throws IOException {

        final CountDownLatch countDown = new CountDownLatch(from.size());

        for (final ObjectSummary summary : from) {
            if (subject.getThrowable() != null) {
                throw new IOException("Can't synchronize users info: " + from);
            }
            try {
                Map.Entry<String, String> spaceAndName = XWikiUser.spaceAndPage(summary.headline);
                getApiManager().getXwikiServicesApi().getFullUserDetails(
                    spaceAndName.getKey(),
                    spaceAndName.getValue()
                ).subscribe(
                    new Action1<XWikiUserFull>() {
                        @Override
                        public void call(XWikiUserFull xWikiUser) {
                            subject.onNext(xWikiUser);
                            countDown.countDown();
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "Can't get user info: " + summary.headline, throwable);
                            if (!HttpException.class.isInstance(throwable) || ((HttpException)throwable).code() != 404) {
                                subject.onError(throwable);
                            }
                            countDown.countDown();
                        }
                    }
                );
            } catch (Exception e) {
                countDown.countDown();
                Log.e(TAG, "Can't synchronize object with id: " + summary.headline, e);
            }
        }

        try {
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * getSyncAllUsers
     * Constants.SYNC_TYPE_ALL_USERS
     * used for getting the all users from server.
     *
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static void getSyncAllUsers(
        final PublishSubject<XWikiUserFull> subject
    ) {
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
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        subject.onError(throwable);
                        semaphore.release();
                    }
                }
            );
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (subject.hasThrowable()) {
            return;
        }

        final CountDownLatch countDown = new CountDownLatch(searchList.size());
        for (final SearchResult item : searchList) {
            if (subject.getThrowable() != null) {// was was not error in sync
                return;
            }
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
                        subject.onNext(xWikiUser);
                        countDown.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Log.e(TAG, "Can't get user", e);
                        if (!HttpException.class.isInstance(e) || ((HttpException) e).code() != 404) {
                            subject.onError(e);
                        }
                        countDown.countDown();
                    }
                }
            );

            // if many users should be synchronized, the task will not be stop
            // even though you close the sync in settings or selecting the "don't sync" option.
            // we should stop the task by checking the sync type each time.
            int syncType = SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.SYNC_TYPE, -1);
            if (syncType != Constants.SYNC_TYPE_ALL_USERS) {
                IOException exception = new IOException("the sync type has been changed");
                subject.onError(exception);
            }
        }
        try {
            countDown.await();
            if (!subject.hasThrowable()) {
                subject.onCompleted();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            subject.onError(e);
        }
    }
}
