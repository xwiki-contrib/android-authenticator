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
