package org.xwiki.android.authenticator.rest;

import org.xwiki.android.authenticator.bean.UserPayload;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;

public class DataManager {

    BaseApiManager baseApiManager;

    public DataManager() {
        baseApiManager = new BaseApiManager();
    }

    public Observable<Response<ResponseBody>> login(String basicAuth) {
        return baseApiManager.getXwikiServicesApi().login(basicAuth);
    }

    public Observable<ResponseBody> updateUser(String wiki, String space, String pageName,
            UserPayload userPayload) {
        return baseApiManager.getXwikiServicesApi().updateUser(wiki, space, pageName, userPayload);
    }
}
