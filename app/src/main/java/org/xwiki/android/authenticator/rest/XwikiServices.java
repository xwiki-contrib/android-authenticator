package org.xwiki.android.authenticator.rest;

import org.xwiki.android.authenticator.bean.UserPayload;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface XWikiServices {

    @POST("bin/login/XWiki/XWikiLogin")
    Observable<Response<ResponseBody>> login(@Header("Authorization") String basicAuth);

    @PUT(ApiEndPoints.REST + ApiEndPoints.WIKIS + "/{wiki}/" + ApiEndPoints.SPACES + "/{space}/" + ApiEndPoints.PAGES + "/{pageName}/" + ApiEndPoints.XWIKI_OBJECTS)
    Observable<ResponseBody> updateUser(
            @Path("wiki") String wiki,
            @Path("space") String space,
            @Path("pageName") String pageName,
            @Body UserPayload userPayload);
}
