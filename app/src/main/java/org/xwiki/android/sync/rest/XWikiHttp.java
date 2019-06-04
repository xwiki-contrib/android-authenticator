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
import okhttp3.Credentials;
import okhttp3.ResponseBody;
import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.bean.ObjectSummary;
import org.xwiki.android.sync.bean.SerachResults.CustomObjectsSummariesContainer;
import org.xwiki.android.sync.bean.XWikiUserFull;
import org.xwiki.android.sync.utils.SharedPrefsUtils;
import retrofit2.HttpException;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

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
        AppContext.Companion.getApiManager().getXwikiServicesApi().login(
            Credentials.basic(username, password)
        ).subscribeOn(
            Schedulers.newThread()
        ).subscribe(
            new Action1<Response<ResponseBody>>() {
                @Override
                public void call(Response<ResponseBody> responseBodyResponse) {
                    if (responseBodyResponse.code() >= 200 && responseBodyResponse.code() <= 209) {
                        String cookie = responseBodyResponse.headers().get("Set-Cookie");
                        SharedPrefsUtils.Companion.putValue(
                            AppContext.Companion.getInstance().getApplicationContext(),
                            Constants.Companion.getCOOKIE(),
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
     * Relogin for account
     *
     * @param context Context to get {@link AccountManager} and other data
     * @param accountName Name of account to know which user must be relogged in
     * @return Observable to know when already authorized
     *
     * @since 0.5
     */
    @Nullable
    public static Observable<String> relogin(
        Context context,
        String accountName
    ) {
        AccountManager accountManager = AccountManager.get(context);

        Account account = null;

        for (Account current : accountManager.getAccounts()) {
            if (current.name.equals(accountName)) {
                account = current;
                break;
            }
        }

        if (account == null) {
            return null;
        } else {
            Observable<String> loginObservable = login(
                accountName,
                accountManager.getPassword(account)
            );
            loginObservable.subscribe(
                new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.d("XWikiHttp", "Relogged in");
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
     *
     * @see Constants#SYNC_TYPE_ALL_USERS
     * @see Constants#SYNC_TYPE_SELECTED_GROUPS
     */
    public static Observable<XWikiUserFull> getSyncData(
        final int syncType,
        final String accountName
    ) {
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
                                getSyncAllUsers(
                                    subject,
                                    accountName
                                );
                            } else if (syncType == Constants.SYNC_TYPE_SELECTED_GROUPS) {
                                List<String> groupIdList = SharedPrefsUtils.Companion.getArrayList(AppContext.Companion.getInstance().getApplicationContext(), Constants.Companion.getSELECTED_GROUPS());
                                getSyncGroups(
                                    groupIdList,
                                    subject,
                                    accountName
                                );
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
     * @param account Account which used for sync
     * @throws IOException Will be thrown if something went wrong
     *
     * @see #getDetailedInfo(List, PublishSubject, String)
     *
     * @since 0.4
     */
    private static void getSyncGroups(
        @NonNull List<String> groupIdList,
        @NonNull final PublishSubject<XWikiUserFull> subject,
        @NonNull final String account

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
            AppContext.Companion.getApiManager().getXwikiServicesApi().getGroupMembers(
                split[0],
                split[1],
                split[2]
            ).subscribe(
                new Action1<CustomObjectsSummariesContainer<ObjectSummary>>() {
                    @Override
                    public void call(CustomObjectsSummariesContainer<ObjectSummary> summaries) {
                        getDetailedInfo(
                            summaries.objectSummaries,
                            subject,
                            account
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
     * @param account Account which used for sync
     *
     * @since 0.4
     */
    private static void getDetailedInfo(
        @NonNull List<ObjectSummary> from,
        @NonNull final PublishSubject<XWikiUserFull> subject,
        @NonNull final String account
    ) {

        final Queue<ObjectSummary> queueOfSummaries = new ArrayDeque<>(from);

        while (subject.getThrowable() == null && !queueOfSummaries.isEmpty()) {
            final ObjectSummary summary = queueOfSummaries.poll();

            try {
                Map.Entry<String, String> spaceAndName = XWikiUserFull.spaceAndPage(summary.getHeadline());
                if (spaceAndName == null) {
                    continue;
                }
                AppContext.Companion.getApiManager().getXwikiServicesApi().getFullUserDetails(
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
                                    XWikiHttp.relogin(
                                        AppContext.Companion.getInstance(),
                                        account
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
            } catch (Exception e) {
                Log.e(TAG, "Can't synchronize object with id: " + summary.getHeadline(), e);
            }
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
        final PublishSubject<XWikiUserFull> subject,
        final String account
    ) {
        final Queue<ObjectSummary> searchList = new ArrayDeque<>();
        final Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
            AppContext.Companion.getApiManager().getXwikiServicesApi().getAllUsersPreview().subscribe(
                new Action1<CustomObjectsSummariesContainer<ObjectSummary>>() {
                    @Override
                    public void call(CustomObjectsSummariesContainer<ObjectSummary> summaries) {
                        searchList.addAll(summaries.objectSummaries);
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
        }

        try {
            semaphore.acquire();

            if (subject.hasThrowable()) {
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "Can't await synchronize all users", e);
        }

        while (subject.getThrowable() == null && !searchList.isEmpty()) {
            final ObjectSummary item = searchList.poll();

            if (subject.getThrowable() != null) {// was was not error in sync
                return;
            }
            AppContext.Companion.getApiManager().getXwikiServicesApi().getFullUserDetails(
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
                                XWikiHttp.relogin(
                                    AppContext.Companion.getInstance(),
                                    account
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
            int syncType = SharedPrefsUtils.Companion.getValue(AppContext.Companion.getInstance().getApplicationContext(), Constants.Companion.getSYNC_TYPE(), -1);
            if (syncType != Constants.SYNC_TYPE_ALL_USERS) {
                IOException exception = new IOException("the sync type has been changed");
                subject.onError(exception);
            }
        }
        if (!subject.hasThrowable()) {
            subject.onCompleted();
        }
    }
}
