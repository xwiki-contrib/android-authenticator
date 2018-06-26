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
package org.xwiki.android.sync.rest;

import android.support.annotation.NonNull;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.bean.ObjectSummary;
import org.xwiki.android.sync.bean.SearchResult;
import org.xwiki.android.sync.bean.SearchResultContainer;
import org.xwiki.android.sync.bean.SerachResults.CustomObjectsSummariesContainer;
import org.xwiki.android.sync.bean.XWikiUser;
import org.xwiki.android.sync.bean.XWikiUserFull;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.xwiki.android.sync.AppContext.getApiManager;

/**
 * Static class which can be used as wrapper for a few requests such as login and other. It
 * contains additional logic which was not added for requests by automatically creating
 * requests services in {@link BaseApiManager}
 *
 * @see BaseApiManager
 * @see XWikiServices
 * @see XWikiPhotosManager
 *
 * @version $Id$
 */
public class XWikiHttp {

    /**
     * Tag for logging
     */
    private static final String TAG = "XWikiHttp";

    /**
     * Provide work with login. If be exactly - create login base credentials, send it via
     * {@link XWikiServices#login(String)}, extract cookies as auth token as a result and send it
     * via returned parameter
     *
     * @param username Username of user
     * @param password Password of user
     * @return Object which can be used for subscribing to results of request. As a result - auth
     * token
     *
     * @since 0.4
     */
    public static Observable<String> login(
        @NonNull String username,
        @NonNull String password
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
                        String cookie = responseBodyResponse.headers().get("Set-Cookie");
                        SharedPrefsUtils.putValue(
                            AppContext.getInstance().getApplicationContext(),
                            Constants.COOKIE,
                            cookie
                        );
                        authTokenSubject.onNext(cookie);
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
     * Create and start procedure with getting full info about users based syncType
     *
     * @param syncType Will be used to understand which of methods of getting users must me used
     * @return Observable which can be observed. In {@link Observer#onNext(Object)} you will
     * receive each user which was correctly received. {@link Observer#onError(Throwable)} will
     * be called on first error of getting users and receiving of users will be stopped.
     * {@link Observer#onCompleted()} will be called when receiving of user was successfully
     * completed
     *
     * @see Constants#SYNC_TYPE_ALL_USERS
     * @see Constants#SYNC_TYPE_SELECTED_GROUPS
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
     * Download and send users in subject for each group in groupIdList. Will call
     * {@link Observer#onCompleted()} when all users from all groups was successfully received
     *
     * @param groupIdList Contains ids of groups to sync
     * @param subject Contains subject to send updates info
     * @throws IOException Will be thrown if something went wrong
     *
     * @see #getDetailedInfo(List, PublishSubject)
     *
     * @since 0.4
     */
    private static void getSyncGroups(
        @NonNull List<String> groupIdList,
        @NonNull final PublishSubject<XWikiUserFull> subject
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
     * Send users full info into subject using from. Will call
     * {@link Observer#onError(Throwable)} on first error of getting user.
     *
     * @param from Objects which can be used to get info about users to load them
     * @param subject Contains subject to send updates info
     *
     * @since 0.4
     */
    private static void getDetailedInfo(
        @NonNull List<ObjectSummary> from,
        @NonNull final PublishSubject<XWikiUserFull> subject
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
            Log.e(TAG, "Can't await completing of getting user detailed info", e);
        }
    }

    /**
     * Start to sync all users using {@link AppContext#getApiManager()}. In
     * {@link Observer#onNext(Object)} you will receive each user which was correctly received.
     * {@link Observer#onError(Throwable)} will be called on first error of getting users and
     * receiving of users will be stopped. {@link Observer#onCompleted()} will be called when
     * receiving of user was successfully completed.
     *
     * @param subject Will be used as object for events
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
            Log.e(TAG, "Can't await synchronize all users", e);
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
            subject.onError(e);
        }
    }
}
