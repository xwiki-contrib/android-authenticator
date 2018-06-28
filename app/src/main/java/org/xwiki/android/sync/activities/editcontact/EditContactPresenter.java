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
package org.xwiki.android.sync.activities.editcontact;

import android.content.Context;
import android.util.Base64;

import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.activities.base.BasePresenter;
import org.xwiki.android.sync.bean.UserPayload;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * {@link BasePresenter} realisation which provide work with MVP. Contains logic for login (in fact
 * - relogin) and update contact info for current user.
 *
 * @version $Id$
 */
public class EditContactPresenter extends BasePresenter<EditContactMvpView> {

    /**
     * Subscription for aggregate all subscriptions and unsubscribe them when it will be need
     * (for example, before destroy)
     */
    private CompositeSubscription subscriptions;

    /**
     * Standard constructor
     * @param context Context for this presenter
     * @see BasePresenter#BasePresenter(Context)
     */
    public EditContactPresenter(Context context) {
        super(context);
        subscriptions = new CompositeSubscription();
    }

    /**
     * Override for unsubscribe all subscriptions which was added to {@link #subscriptions}
     */
    @Override
    public void detachView() {
        super.detachView();
        subscriptions.unsubscribe();
    }

    /**
     * Safely clear {@link #subscriptions} list.
     */
    public void clearSubscription() {
        subscriptions.clear();
    }

    /**
     * Init calling to update contact info on server.
     *
     * @param wiki Wiki context
     * @param space Wiki space
     * @param pageName In fact - username of contact on wiki
     * @param userPayload Data to update
     */
    public void updateUser(String wiki, String space, String pageName, UserPayload userPayload) {
        checkViewAttached();
        getMvpView().showProgress();
        subscriptions.add(
            AppContext.getApiManager().getXwikiServicesApi().updateUser(
                wiki,
                space,
                pageName,
                userPayload.getFirstName(),
                userPayload.getLastName(),
                userPayload.getEmail(),
                userPayload.getPhone()
            ).subscribeOn(
                Schedulers.computation()
            ).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe(
                new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        try {
                            if (e instanceof HttpException) {
                                int code = ((HttpException) e).code();
                                if (code == 401) {
                                    getMvpView().showErrorOnUpdatingContact();
                                }
                            }
                        } catch (Throwable throwable) {
                            RxJavaPlugins.getInstance().getErrorHandler().handleError(throwable);
                        }
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        getMvpView().hideProgress();
                        getMvpView().showContactUpdateSuccessfully();
                    }
                }
            )
        );
    }

    /**
     * login.
     *
     * @param username user's name
     * @param password user's password
     */
    public void login(String username, String password) {
        checkViewAttached();
        getMvpView().showProgress();
        String basicAuth = username + ":" + password;
        basicAuth = "Basic " + Base64.encodeToString(basicAuth.getBytes(), Base64.NO_WRAP);
        subscriptions.add(
            AppContext.getApiManager().getXwikiServicesApi().login(
                basicAuth
            ).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe(
                new Subscriber<Response<ResponseBody>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorLogin();
                    }

                    @Override
                    public void onNext(Response<ResponseBody> responseBodyResponse) {
                        getMvpView().hideProgress();
                        getMvpView().showLoginSuccessfully(responseBodyResponse);
                    }
                }
            )
        );
    }

}
