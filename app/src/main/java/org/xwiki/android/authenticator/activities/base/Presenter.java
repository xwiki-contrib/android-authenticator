package org.xwiki.android.authenticator.activities.base;

public interface Presenter<V extends MVPView> {

    void attachView(V mvpView);

    void detachView();

}
