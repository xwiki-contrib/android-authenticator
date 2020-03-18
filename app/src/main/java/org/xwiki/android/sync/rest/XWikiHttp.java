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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xwiki.android.sync.bean.ObjectSummary;
import org.xwiki.android.sync.bean.SearchResults.CustomObjectsSummariesContainer;
import org.xwiki.android.sync.bean.XWikiUserFull;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import kotlin.Pair;
import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static org.xwiki.android.sync.AppContextKt.getAppContext;
import static org.xwiki.android.sync.ConstantsKt.SYNC_TYPE_ALL_USERS;
import static org.xwiki.android.sync.ConstantsKt.SYNC_TYPE_SELECTED_GROUPS;
import static org.xwiki.android.sync.utils.JavaCoroutinesBindingsKt.getUserAccountName;
import static org.xwiki.android.sync.utils.JavaCoroutinesBindingsKt.getUserSyncType;

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

    private final BaseApiManager apiManager;
    private final Long userAccountId;
    private final String userAccountName;

    public XWikiHttp(BaseApiManager apiManager, Long userAccountId) {
        this.apiManager = apiManager;
        this.userAccountId = userAccountId;
        this.userAccountName = getUserAccountName(userAccountId);
    }

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
    public Observable<String> login(
        @NonNull String username,
        @NonNull String password
    ) {
        final PublishSubject<String> authTokenSubject = PublishSubject.create();
        apiManager.getXwikiServicesApi().login(
            Credentials.basic(username, password)
        ).subscribeOn(
            Schedulers.newThread()
        ).subscribe(
            new Action1<Response<ResponseBody>>() {
                @Override
                public void call(Response<ResponseBody> responseBodyResponse) {
                    if (responseBodyResponse.code() >= 200 && responseBodyResponse.code() <= 209) {
                        String cookie = responseBodyResponse.headers().get("Set-Cookie");
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
     * Relogin for account
     *
     * @param context Context to get {@link AccountManager} and other data
     * @return Observable to know when already authorized
     *
     * @since 0.5
     */
    @Nullable
    public Observable<String> relogin(
        Context context
    ) {
        AccountManager accountManager = AccountManager.get(context);

        Account account = null;

        for (Account current : accountManager.getAccounts()) {
            if (current.name.equals(userAccountName)) {
                account = current;
                break;
            }
        }

        if (account == null) {
            return null;
        } else {
            Observable<String> loginObservable = login(
                account.name,
                accountManager.getPassword(account)
            );
            loginObservable.subscribe(
                    new Action1<String>() {
                        @Override
                        public void call(String s) {
                            Log.d("XWikiHttp", "Relogged in");
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "Failed to relogin", throwable);
                        }
                    }
            );
            return loginObservable;
        }
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
     */
    public Pair<Subject<XWikiUserFull, XWikiUserFull>, Thread> getSyncData(
        final int syncType,
        final List<String> selectedGroups
    ) {
        final PublishSubject<XWikiUserFull> subject = PublishSubject.create();
        Thread newThread = null;
        final Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
            newThread = new Thread(
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
                            if (syncType == SYNC_TYPE_ALL_USERS) {
                                getSyncAllUsers(
                                    subject
                                );
                            } else if (syncType == SYNC_TYPE_SELECTED_GROUPS) {
                                getSyncGroups(
                                    selectedGroups,
                                    subject
                                );
                            } else {
                                throw new IOException(TAG + "syncType error, SyncType=" + syncType);
                            }
                        } catch (IOException e) {
                            subject.onError(e);
                        }
                    }
                }
            );
            newThread.start();
            semaphore.acquire();
        } finally {
            return new Pair(subject, newThread);
        }
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
    private void getSyncGroups(
        @NonNull List<String> groupIdList,
        @NonNull final PublishSubject<XWikiUserFull> subject

    ) throws IOException {
        final CountDownLatch groupsCountDown = new CountDownLatch(groupIdList.size());
        for (String groupId : groupIdList) {
            if (subject.getThrowable() != null) {
                return;
            }
            String[] split = XWikiUserFull.splitId(groupId);
            if (split == null) {
                IOException exception = new IOException(TAG + ",in getSyncGroups, groupId error");
                subject.onError(exception);
                throw exception;
            }
            apiManager.getXwikiServicesApi().getGroupMembers(
                split[0],
                split[1],
                split[2]
            ).subscribe(
                new Action1<CustomObjectsSummariesContainer<ObjectSummary>>() {
                    @Override
                    public void call(CustomObjectsSummariesContainer<ObjectSummary> summaries) {
                        getDetailedInfo(
                            summaries.getObjectSummaries(),
                            subject
                        );
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
        try {
            groupsCountDown.await();
            subject.onCompleted();
        } catch (InterruptedException e) {
            subject.onError(e);
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
    private void getDetailedInfo(
        @NonNull List<ObjectSummary> from,
        @NonNull final PublishSubject<XWikiUserFull> subject
    ) {

        final Queue<ObjectSummary> queueOfSummaries = new ArrayDeque<>(from);

        while (subject.getThrowable() == null && !queueOfSummaries.isEmpty()) {
            final ObjectSummary summary = queueOfSummaries.poll();

            try {
                if (summary.getHeadline() != null && !summary.getHeadline().isEmpty()) {
                    Map.Entry<String, String> spaceAndName = XWikiUserFull.spaceAndPage(summary.getHeadline());
                    if (spaceAndName == null) {
                        continue;
                    }
                    apiManager.getXwikiServicesApi().getFullUserDetails(
                            spaceAndName.getKey(),
                            spaceAndName.getValue()
                    ).subscribe(
                            new Observer<XWikiUserFull>() {
                                @Override
                                public void onCompleted() { }

                                @Override
                                public void onError(Throwable e) {
                                    try {
                                        HttpException asHttpException = (HttpException) e;
                                        if (asHttpException.code() == 401) {//Unauthorized
                                            relogin(
                                                    getAppContext()
                                            ).subscribe(
                                                    new Observer<String>() {
                                                        @Override
                                                        public void onCompleted() {
                                                            queueOfSummaries.offer(summary);
                                                        }

                                                        @Override
                                                        public void onError(Throwable e) {
                                                            subject.onError(e);
                                                        }

                                                        @Override
                                                        public void onNext(String s) {
                                                            Log.d(TAG, "Relogged in");
                                                        }
                                                    }
                                            );
                                            return;
                                        } else {
                                            if (asHttpException.code() == 404) {
                                                return;
                                            }
                                        }
                                    } catch (ClassCastException e1) {
                                        Log.e(TAG, "Can't cast exception to HttpException", e1);
                                    }
                                    subject.onError(e);
                                }

                                @Override
                                public void onNext(XWikiUserFull userFull) {
                                    subject.onNext(userFull);
                                }
                            }
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Can't synchronize object with id: " + summary.getHeadline(), e);
            }
        }
    }

    /**
     * Start to sync all users. In
     * {@link Observer#onNext(Object)} you will receive each user which was correctly received.
     * {@link Observer#onError(Throwable)} will be called on first error of getting users and
     * receiving of users will be stopped. {@link Observer#onCompleted()} will be called when
     * receiving of user was successfully completed.
     *
     * @param subject Will be used as object for events
     */
    private void getSyncAllUsers(
        final PublishSubject<XWikiUserFull> subject
    ) {
        final Queue<ObjectSummary> searchList = new ArrayDeque<>();
        final Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
            apiManager.getXwikiServicesApi().getAllUsersPreview().subscribe(
                new Action1<CustomObjectsSummariesContainer<ObjectSummary>>() {
                    @Override
                    public void call(CustomObjectsSummariesContainer<ObjectSummary> summaries) {
                        searchList.addAll(summaries.getObjectSummaries());
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
        } catch (InterruptedException e) {
            Log.e(TAG, "Can't await synchronize all users", e);
            return;
        }

        try {
            semaphore.acquire();

            if (subject.hasThrowable()) {
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "Can't await synchronize all users", e);
            return;
        }

        while (subject.getThrowable() == null && !searchList.isEmpty()) {
            final ObjectSummary item = searchList.poll();

            if (subject.getThrowable() != null) {// was was not error in sync
                return;
            }
            apiManager.getXwikiServicesApi().getFullUserDetails(
                item.getWiki(),
                item.getSpace(),
                item.getPageName()
            ).subscribe(
                new Observer<XWikiUserFull>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Can't get user", e);
                        try {
                            HttpException asHttpException = (HttpException) e;
                            if (asHttpException.code() == 401) {//Unauthorized
                                relogin(
                                        getAppContext()
                                ).subscribe(
                                    new Observer<String>() {
                                        @Override
                                        public void onCompleted() {
                                            searchList.offer(item);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            subject.onError(e);
                                        }

                                        @Override
                                        public void onNext(String s) {
                                            Log.d(TAG, "Relogged in");
                                        }
                                    }
                                );
                                return;
                            } else {
                                if (asHttpException.code() == 404) {
                                    return;
                                }
                            }
                        } catch (ClassCastException e1) {
                            Log.e(TAG, "Can't cast exception to HttpException", e1);
                        }
                        subject.onError(e);
                    }

                    @Override
                    public void onNext(XWikiUserFull userFull) {
                        subject.onNext(userFull);
                    }
                }
            );

            // if many users should be synchronized, the task will not be stop
            // even though you close the sync in settings or selecting the "don't sync" option.
            // we should stop the task by checking the sync type each time.
            int syncType = getUserSyncType(userAccountId);
            if (syncType != SYNC_TYPE_ALL_USERS) {
                IOException exception = new IOException("the sync type has been changed");
                subject.onError(exception);
            }
        }
        if (!subject.hasThrowable()) {
            subject.onCompleted();
        }
    }
}
