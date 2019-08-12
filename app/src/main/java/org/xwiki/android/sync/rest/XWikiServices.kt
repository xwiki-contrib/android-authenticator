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
package org.xwiki.android.sync.rest

import okhttp3.ResponseBody
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.SearchResultContainer
import org.xwiki.android.sync.bean.SerachResults.CustomObjectsSummariesContainer
import org.xwiki.android.sync.bean.SerachResults.CustomSearchResultContainer
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.bean.XWikiUserFull
import retrofit2.Response
import retrofit2.http.*
import rx.Observable

import org.xwiki.android.sync.rest.ApiEndPoints.SPACES

/**
 * Interface for interacting with XWiki services
 *
 * @see retrofit2.Retrofit.create
 * @see [Retrofit docs](http://square.github.io/retrofit/)
 *
 *
 * @version $Id: 11bd27975839f5a0e569a5e7abcdcab8ee60b6c3 $
 */
interface XWikiServices {

    /**
     * @since 0.4
     */
    @get:GET(
        ApiEndPoints.REST +
                ApiEndPoints.WIKIS +
                "/xwiki/classes/XWiki.XWikiUsers/objects"
    )
    val allUsersPreview: Observable<CustomObjectsSummariesContainer<ObjectSummary>>

    @POST("bin/login/XWiki/XWikiLogin")
    fun login(@Header("Authorization") basicAuth: String): Observable<Response<ResponseBody>>

    @FormUrlEncoded
    @PUT(ApiEndPoints.REST + ApiEndPoints.WIKIS + "/{wiki}/" + SPACES + "/{space}/" + ApiEndPoints.PAGES + "/{pageName}/" + ApiEndPoints.XWIKI_OBJECTS)
    fun updateUser(
        @Path("wiki") wiki: String,
        @Path("space") space: String,
        @Path("pageName") pageName: String,
        @Field("property#first_name") firstName: String?,
        @Field("property#last_name") lastName: String?,
        @Field("property#email") email: String?,
        @Field("property#phone") phone: String?,
        @Field("property#address") address: String?,
        @Field("property#company") company: String?,
        @Field("property#comment") comment: String?
    ): Observable<XWikiUserFull>

    /**
     * @since 0.4
     */
    @GET(ApiEndPoints.REST + ApiEndPoints.WIKIS + "/query?q=object:XWiki.XWikiGroups")
    fun availableGroups(
        @Query("number") number: Int?
    ): Observable<CustomSearchResultContainer<XWikiGroup>>

    @GET(
        ApiEndPoints.REST +
                ApiEndPoints.WIKIS +
                "/xwiki/classes/XWiki.XWikiUsers/objects"
    )
    fun getAllUsersListByOffset (
        @Query("start") offset: Int?,
        @Query("number") limit: Int?
    ): Observable<CustomObjectsSummariesContainer<ObjectSummary>>

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
                ApiEndPoints.WIKIS +
                "/xwiki/" +
                ApiEndPoints.SPACES +
                "/{space}/" +
                ApiEndPoints.PAGES +
                "/{name}/objects/XWiki.XWikiUsers/0"
    )
    fun getFullUserDetails(
        @Path("space") space: String,
        @Path("name") name: String
    ): Observable<XWikiUserFull>

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
                ApiEndPoints.WIKIS +
                "/{wiki}/" +
                ApiEndPoints.SPACES +
                "/{space}/" +
                ApiEndPoints.PAGES +
                "/{name}/objects/XWiki.XWikiUsers/0"
    )
    fun getFullUserDetails(
        @Path("wiki") wiki: String,
        @Path("space") space: String,
        @Path("name") name: String
    ): Observable<XWikiUserFull>

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
                ApiEndPoints.WIKIS +
                "/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers" +
                "&number={count}" +
                "&start={offset}"
    )
    fun getUsersPreview(
        @Query("number") count: Int?,
        @Query("start") offset: Int?
    ): Observable<SearchResultContainer>

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
                ApiEndPoints.WIKIS +
                "/{wiki}/" +
                ApiEndPoints.SPACES +
                "/{space}/" +
                ApiEndPoints.PAGES +
                "/{name}/objects/XWiki.XWikiGroups"
    )
    fun getGroupMembers(
        @Path("wiki") wiki: String,
        @Path("space") space: String,
        @Path("name") name: String
    ): Observable<CustomObjectsSummariesContainer<ObjectSummary>>
}
