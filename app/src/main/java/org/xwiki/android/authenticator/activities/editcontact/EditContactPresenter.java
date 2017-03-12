package org.xwiki.android.authenticator.activities.editcontact;

import android.content.Context;
import android.util.Base64;

import org.xwiki.android.authenticator.activities.base.BasePresenter;
import org.xwiki.android.authenticator.bean.UserPayload;
import org.xwiki.android.authenticator.rest.DataManager;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EditContactPresenter extends BasePresenter<EditContactMvpView> {

    private final DataManager dataManager;
    private CompositeSubscription subscriptions;

    public EditContactPresenter(Context context) {
        super(context);
        dataManager = new DataManager();
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EditContactMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        subscriptions.unsubscribe();
    }

    public void updateUser(String wiki, String space, String pageName, UserPayload userPayload) {
        checkViewAttached();
        getMvpView().showProgress();
        subscriptions.add(dataManager.updateUser(wiki, space, pageName, userPayload)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
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
                })
        );
    }

    /**
     * login
     *
     * @param username user's name
     * @param password user's password
     */
    public void login(String username, String password) {
        checkViewAttached();
        getMvpView().showProgress();
        String basicAuth = username + ":" + password;
        basicAuth = "Basic " + Base64.encodeToString(basicAuth.getBytes(), Base64.NO_WRAP);
        subscriptions.add(dataManager.login(basicAuth)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Response<ResponseBody>>() {
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
                })

        );
    }

}
