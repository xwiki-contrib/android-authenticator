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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.SerachResults.CustomObjectsSummariesContainer;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.bean.XWikiUserFull;
import org.xwiki.android.authenticator.utils.ImageUtils;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import rx.functions.Action1;
import rx.functions.Func1;

import static org.xwiki.android.authenticator.AppContext.getApiManager;

/**
 * XWikiHttp
 */
public class XWikiHttp {
    private static final String TAG = "XWikiHttp";
    private static String serverRestPreUrl = null;


    /**
     * Sign Up
     *
     * @param userId    Required. user id which is used for login.
     * @param password  Required. user's password
     * @param formToken Required. the form token which is initialized before sign up
     * @param captcha   Required if needed. the user's input captcha
     * @param firstName Not Required. user's firstName
     * @param lastName  Not Required. user's lastName
     * @param email     Not Required. user's email
     * @return Boolean
     * true:  sign up successfully
     * false: sign up unsuccessfully
     * @throws IOException //String registerUrl = "http://210.76.192.253:8080/xwiki/bin/view/XWiki/Registration";
     */
    public static HttpResponse signUp(
        String userId, String password,
        String formToken,
        String captcha,
        String firstName,
        String lastName,
        String email
    ) throws IOException {
        String registerUrl = getServerAddress() + "/bin/view/XWiki/Registration";
        if (registerUrl.contains("www.xwiki.org")) {
            registerUrl = getServerAddress() + "/bin/view/XWiki/RealRegistration";
        }
        HttpRequest request = new HttpRequest(registerUrl, HttpRequest.HttpMethod.POST, null);
        HttpExecutor httpExecutor = new HttpExecutor();
        request.httpParams.putBodyParams("form_token", formToken);
        request.httpParams.putBodyParams("parent", "xwiki:Main.UserDirectory");
        request.httpParams.putBodyParams("register_first_name", firstName);
        request.httpParams.putBodyParams("register_last_name", lastName);
        request.httpParams.putBodyParams("xwikiname", userId);
        request.httpParams.putBodyParams("register_password", password);
        request.httpParams.putBodyParams("register2_password", password);
        request.httpParams.putBodyParams("register_email", email);
        request.httpParams.putBodyParams("captcha_answer", captcha);
        request.httpParams.putBodyParams("template", "XWiki.XWikiUserTemplate");
        request.httpParams.putBodyParams("xredirect", "/xwiki/bin/view/Main/UserDirectory");
        HttpResponse response = httpExecutor.performRequest(request);
        return response;

        /*
        formToken = document.select("input[name=template]").val();
        if (TextUtils.isEmpty(formToken)) {
            return true;
        }
        */
        //return false;
    }

    /**
     * before sign up, we should init the cookie, get the form token and the captcha.
     *
     * @return String
     * the form token
     * @throws IOException
     */
    public static HttpResponse signUpInitCookieForm() throws IOException {
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, null);
        String registerUrl = getServerAddress() + "/bin/view/XWiki/Registration";
        if (registerUrl.contains("www.xwiki.org")) {
            registerUrl = getServerAddress() + "/bin/view/XWiki/RealRegistration";
        }
        HttpRequest request = new HttpRequest(registerUrl);
        HttpExecutor httpExecutor = new HttpExecutor();
        HttpResponse response = httpExecutor.performRequest(request);
        return response;
    }


    /**
     * sign up
     *
     * @param userId
     * @param password
     * @param formToken
     * @return
     */
    public static HttpResponse signUp(String userId, String password, String formToken, String captcha) throws IOException {
        return signUp(userId, password, formToken, captcha, "", "", "");
    }


    /**
     * get user information
     *
     * @param id curriki:XWiki.Luisafan
     * @return XWikiUser
     * Without Getting LastModifiedDate
     * @throws IOException
     */
    public static XWikiUser getUserDetail(String id) throws IOException, XmlPullParserException {
        String[] split = XWikiUser.splitId(id);
        if (split == null) throw new IOException(TAG + ",in getUserDetail, userId error");
        return getUserDetail(split[0], split[1], split[2]);
    }


    /**
     * get user information
     *
     * @param wiki
     * @param space
     * @param name
     * @return XWikiUser
     * Without Getting LastModifiedDate
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static XWikiUser getUserDetail(String wiki, String space, String name) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/" + wiki + "/spaces/" + space + "/pages/" + name + "/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url);
        HttpExecutor httpExecutor = new HttpExecutor();
        HttpResponse response = httpExecutor.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode == 404) {
            return null;
        }
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode=" + statusCode + ",response=" + response.getResponseMessage());
        }
        XWikiUser user = XmlUtils.getXWikiUser(new ByteArrayInputStream(response.getContentData()));
        return user;
    }

    /**
     * update user information
     *
     * @param user
     * @return true:update success, false:update fail
     * @throws IOException curl -u fitz:fitz2xwiki -X PUT -H "Content-type: application/x-www-form-urlencoded" -d "className=XWiki.XWikiUsers" -d "property#company=iiedacas" http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/fitz/objects/XWiki.XWikiUsers/0
     */
    public static HttpResponse updateUser(XWikiUser user) throws IOException {
        String url = getServerRestUrl() + "/wikis/" + user.wiki + "/spaces/" + user.space + "/pages/" + user.pageName + "/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.PUT, null);
        request.httpParams.putBodyParams("className", "XWiki.XWikiUsers");
        request.httpParams.putBodyParams("property#first_name", user.firstName);
        request.httpParams.putBodyParams("property#last_name", user.lastName);
        request.httpParams.putBodyParams("property#email", user.email);
        request.httpParams.putBodyParams("property#phone", user.phone);
        HttpExecutor httpExecutor = new HttpExecutor();
        HttpResponse response = httpExecutor.performRequest(request);
        return response;
    }

    public static class SyncData {
        //the users which have been modified from the last sync time. mainly used for updating and adding..
        private List<XWikiUserFull> updateUserList;
        //all the users in the server or all the users of the selected groups used for deleting.
        private HashSet<String> allIdSet;

        public SyncData() {
            updateUserList = new ArrayList<>();
            allIdSet = new HashSet<>();
        }

        public List<XWikiUserFull> getUpdateUserList() {
            return updateUserList;
        }

        public HashSet<String> getAllIdSet() {
            return allIdSet;
        }

        @Override
        public String toString() {
            return "SyncData{" +
                "updateUserList=" + updateUserList +
                ", allIdSet=" + allIdSet +
                '}';
        }
    }


    /**
     * getSyncData
     * get SyncData used in SyncAdapter.onPerformSync
     *
     * @param lastSyncTime the last sync time.
     * @param syncType     Constants.SYNC_TYPE_ALL_USERS
     *                     Constants.SYNC_TYPE_SELECTED_GROUPS
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static SyncData getSyncData(String lastSyncTime, int syncType) throws IOException, XmlPullParserException {
        if (syncType == Constants.SYNC_TYPE_ALL_USERS) {
            return getSyncAllUsers(lastSyncTime);
        } else if (syncType == Constants.SYNC_TYPE_SELECTED_GROUPS) {
            List<String> groupIdList = SharedPrefsUtils.getArrayList(AppContext.getInstance().getApplicationContext(), Constants.SELECTED_GROUPS);
            return getSyncGroups(groupIdList, lastSyncTime);
        }
        throw new IOException(TAG + "syncType error, SyncType=" + syncType);
    }

    /**
     * getSyncGroups
     * Constants.SYNC_TYPE_SELECTED_GROUPS
     * get the selected groups' SyncData
     *
     * @param groupIdList  the selected groups' id list.
     * @param lastSyncTime the last sync time.
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static SyncData getSyncGroups(List<String> groupIdList, String lastSyncTime) throws IOException, XmlPullParserException {
        if (groupIdList == null || groupIdList.size() == 0) return null;
        final SyncData syncData = new SyncData();
        final CountDownLatch groupsCountDown = new CountDownLatch(groupIdList.size());
        for (String groupId : groupIdList) {
            String[] split = XWikiUser.splitId(groupId);
            if (split == null) throw new IOException(TAG + ",in getSyncGroups, groupId error");
            getApiManager().getXwikiServicesApi().getGroupMembers(
                split[0],
                split[1],
                split[2]
            ).map(
                // Map<String, String> : keys - usernames, values - spaces
                new Func1<CustomObjectsSummariesContainer<ObjectSummary>, Map<String, String>>() {
                    @Override
                    public Map<String, String> call(CustomObjectsSummariesContainer<ObjectSummary> xWikiUserCustomObjectsSummariesContainer) {
                        Map<String, String> pairs = new HashMap<>();

                        for (ObjectSummary summary : xWikiUserCustomObjectsSummariesContainer.objectSummaries) {
                            try {
                                Map.Entry<String, String> spaceAndName = XWikiUser.spaceAndPage(summary.headline);
                                pairs.put(
                                    spaceAndName.getValue(),
                                    spaceAndName.getKey()
                                );
                            } catch (Exception e) {
                                Log.e(TAG, "Can't transform group member headline", e);
                            }
                        }

                        return pairs;
                    }
                }
            ).subscribe(
                new Action1<Map<String, String>>() {
                    @Override
                    public void call(Map<String, String> pairs) {
                        syncData.getUpdateUserList().addAll(
                            getDetailedInfo(
                                pairs
                            )
                        );
                        groupsCountDown.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        groupsCountDown.countDown();
                    }
                }
            );
        }
        try {
            groupsCountDown.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "Can't await update of user", e);
        }
        return syncData;
    }

    /**
     * @param from Key-value pairs where key - username, value - space
     * @return List of users
     */
    private static List<XWikiUserFull> getDetailedInfo(Map<String, String> from) {
        final List<XWikiUserFull> users = new ArrayList<>();

        final CountDownLatch countDown = new CountDownLatch(from.size());

        for (final String userPage : from.keySet()) {
            getApiManager().getXwikiServicesApi().getFullUserDetails(
                from.get(userPage),
                userPage
            ).subscribe(
                new Action1<XWikiUserFull>() {
                    @Override
                    public void call(XWikiUserFull xWikiUser) {
                        users.add(xWikiUser);
                        countDown.countDown();
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Can't get user info", throwable);
                        countDown.countDown();
                    }
                }
            );
        }

        try {
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * getSyncAllUsers
     * Constants.SYNC_TYPE_ALL_USERS
     * used for getting the all users from server.
     *
     * @param lastSyncTime the last sync time.
     * @return SyncData
     * the SyncData can be used in updating the data.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static SyncData getSyncAllUsers(String lastSyncTime) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=" + Constants.LIMIT_MAX_SYNC_USERS;
        HttpResponse response = new HttpExecutor().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode=" + statusCode + ",response=" + response.getResponseMessage());
        }
        List<SearchResult> searchList = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        final SyncData syncData = new SyncData();
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = null;
        final CountDownLatch countDown = new CountDownLatch(searchList.size());
        for (SearchResult item : searchList) {
            syncData.allIdSet.add(item.id);
            itemDate = StringUtils.iso8601ToDate(item.modified);
            if (itemDate.before(lastSynDate)) {
                countDown.countDown();
                continue;
            } else {
                String[] splitted = XWikiUser.splitId(item.id);
                String wiki = splitted[0];
                String space = splitted[1];
                String pageName = splitted[2];
                getApiManager().getXwikiServicesApi().getFullUserDetails(
                    wiki,
                    space,
                    pageName
                ).subscribe(
                    new Action1<XWikiUserFull>() {
                        @Override
                        public void call(XWikiUserFull xWikiUser) {
                            syncData.getUpdateUserList().add(xWikiUser);
                            countDown.countDown();
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable e) {
                            Log.e(TAG, "Can't get user", e);
                            countDown.countDown();
                        }
                    }
                );
            }

            // if many users should be synchronized, the task will not be stop
            // even though you close the sync in settings or selecting the "don't sync" option.
            // we should stop the task by checking the sync type each time.
            int syncType = SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.SYNC_TYPE, -1);
            if (syncType != Constants.SYNC_TYPE_ALL_USERS) {
                throw new IOException("the sync type has been changed");
            } else {
                countDown.countDown();
            }
        }
        try {
            countDown.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "Can't perform `getSyncAllUsers`", e);
        }
        return syncData;
    }

    /**
     * getSyncAllUsersSimple
     *
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static List<SearchResult> getSyncAllUsersSimple() throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=" + Constants.LIMIT_MAX_SYNC_USERS;
        HttpResponse response = new HttpExecutor().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode=" + statusCode + ",response=" + response.getResponseMessage());
        }
        List<SearchResult> searchList = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        return searchList;
    }

    /**
     * getServerRestUrl
     * get serverRestPreUrl from preference.
     * http://www.xwiki.org/xwiki + "/rest"
     *
     * @return String
     * url
     */
    public static String getServerRestUrl() {
        if (serverRestPreUrl == null) {
            serverRestPreUrl = getServerAddress() + "/rest";
        }
        return serverRestPreUrl;
    }

    public static String getServerAddress() {
        return SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.SERVER_ADDRESS, null);
    }

    public static void setRestUrlNULL() {
        serverRestPreUrl = null;
    }

    /**
     * downloadImage
     * download the avatar photo of contact
     *
     * @param user       the user's pageName
     * @param avatarName the avatar name like jvelociter.jpg
     * @return http://www.xwiki.org/xwiki/bin/download/XWiki/jvelociter/jvelociter.jpg
     */
    public static byte[] downloadImage(String user, String avatarName) throws IOException {
        String url = getServerAddress() + "/bin/download/XWiki/" + user + "/" + avatarName;
        return downloadImage(url);
    }

    /**
     * Download the image from the server.
     *
     * @param avatarUrl the URL pointing to the avatar image
     * @return a byte array with the raw JPEG avatar image
     */
    public static byte[] downloadImage(final String avatarUrl) throws IOException {
        // If there is no avatar, we're done
        if (TextUtils.isEmpty(avatarUrl)) {
            return null;
        }
        Log.i(TAG, "Downloading avatar: " + avatarUrl);
        // Request the avatar image from the server, and create a bitmap
        // object from the stream we get back.
        HttpRequest request = new HttpRequest(avatarUrl);
        HttpExecutor httpExecutor = new HttpExecutor();
        HttpResponse response = httpExecutor.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode == 404) {
            return null;
        } else if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode=" + statusCode + ",response=" + response.getResponseMessage());
        }
        //byte[] bytes = response.getContentData() maybe should less than 8M.  now only 6M has been tested.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new ByteArrayInputStream(response.getContentData()), null, options);
        Bitmap avatar = null;
        //calc if the avatar bitmap memory are more than 4096 * 1024 B = 4MB
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 1;
        //if the memory size of the image are more than 4M options.inSampleSize>1.
        int size = height * width * 2; //2 is RGB_565
        int reqSize = 4096 * 1024;
        if (size > reqSize) {
            final int halfSize = size / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfSize / inSampleSize) > reqSize) {
                inSampleSize *= 2;
            }
            options.inSampleSize = inSampleSize;
        }
        avatar = BitmapFactory.decodeStream(new ByteArrayInputStream(response.getContentData()), null, options);
        //ensure < 1M.  avoid transactionException when storing in local database.
        avatar = ImageUtils.compressByQuality(avatar, 960);
        // Take the image we received from the server, whatever format it
        // happens to be in, and convert it to a JPEG image. Note: we're
        // not resizing the avatar - we assume that the image we get from
        // the server is a reasonable size...
        //return byte[] value
        if (avatar == null) return null;
        ByteArrayOutputStream convertStream = new ByteArrayOutputStream();
        avatar.compress(Bitmap.CompressFormat.PNG, 100, convertStream);
        convertStream.flush();
        //it's important to call recycle on bitmaps
        avatar.recycle();
        return convertStream.toByteArray();
    }

}
