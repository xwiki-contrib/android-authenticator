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

import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.SearchResultContainer;
import org.xwiki.android.authenticator.bean.SerachResults.CustomObjectsSummariesContainer;
import org.xwiki.android.authenticator.bean.SerachResults.CustomSearchResultContainer;
import org.xwiki.android.authenticator.bean.UserPayload;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.bean.XWikiUserFull;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

import static org.xwiki.android.authenticator.rest.ApiEndPoints.SPACES;

public interface XWikiServices {

    @POST("bin/login/XWiki/XWikiLogin")
    Observable<Response<ResponseBody>> login(@Header("Authorization") String basicAuth);

    @PUT(ApiEndPoints.REST + ApiEndPoints.WIKIS + "/{wiki}/" + SPACES + "/{space}/" + ApiEndPoints.PAGES + "/{pageName}/" + ApiEndPoints.XWIKI_OBJECTS)
    Observable<ResponseBody> updateUser(
            @Path("wiki") String wiki,
            @Path("space") String space,
            @Path("pageName") String pageName,
            @Body UserPayload userPayload
    );

    @GET(ApiEndPoints.REST + ApiEndPoints.WIKIS + "/query?q=object:XWiki.XWikiGroups")
    Observable<CustomSearchResultContainer<XWikiGroup>> availableGroups(
            @Query("number") Integer number
    );

    @GET(
            ApiEndPoints.REST +
                    ApiEndPoints.WIKIS +
                    "/{wiki}/" +
                    ApiEndPoints.SPACES +
                    "/{space}/" +
                    ApiEndPoints.PAGES +
                    "/{name}"
    )
    Observable<XWikiUser> getUserDetails(
            @Path("wiki") String wiki,
            @Path("space") String space,
            @Path("name") String name
    );

    @GET(
            ApiEndPoints.REST +
                    ApiEndPoints.WIKIS +
                    "/xwiki/" +
                    ApiEndPoints.SPACES +
                    "/{space}/" +
                    ApiEndPoints.PAGES +
                    "/{name}"
    )
    Observable<XWikiUser> getUserDetails(
            @Path("space") String space,
            @Path("name") String name
    );

    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/xwiki/" +
            ApiEndPoints.SPACES +
            "/{space}/" +
            ApiEndPoints.PAGES +
            "/{name}/objects/XWiki.XWikiUsers/0"
    )
    Observable<XWikiUserFull> getFullUserDetails(
        @Path("space") String space,
        @Path("name") String name
    );

    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/{wiki}/" +
            ApiEndPoints.SPACES +
            "/{space}/" +
            ApiEndPoints.PAGES +
            "/{name}/objects/XWiki.XWikiUsers/0"
    )
    Observable<XWikiUserFull> getFullUserDetails(
        @Path("wiki") String wiki,
        @Path("space") String space,
        @Path("name") String name
    );

    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=" + Integer.MAX_VALUE
    )
    Observable<SearchResultContainer> getAllUsersPreview();

    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers" +
            "&number={count}" +
            "&start={offset}"
    )
    Observable<SearchResultContainer> getUsersPreview(
        @Query("number") Integer count,
        @Query("start") Integer offset
    );

    @GET(
            ApiEndPoints.REST +
                    ApiEndPoints.WIKIS +
                    "/{wiki}/" +
                    ApiEndPoints.SPACES +
                    "/{space}/" +
                    ApiEndPoints.PAGES +
                    "/{name}/objects/XWiki.XWikiGroups"
    )
    Observable<CustomObjectsSummariesContainer<ObjectSummary>> getGroupMembers(
            @Path("wiki") String wiki,
            @Path("space") String space,
            @Path("name") String name
    );
}
