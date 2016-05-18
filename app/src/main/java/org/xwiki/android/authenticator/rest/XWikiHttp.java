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
import android.util.Base64;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.Page;
import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.utils.ImageUtils;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by fitz on 2016/4/25.
 */
public class XWikiHttp {
    //TODO optimize the http request
    //Q1 exception
    //Q2 cache
    //Q3 query performance
    private static final String TAG = "XWikiHttp";

    private static String serverRestPreUrl = null;

    /**
     * login
     * @param username
     * @param password
     * @return Body byte[]
     * @throws IOException
     * http://localhost:8080/xwiki/bin/login/XWiki/XWikiLogin
     */
    public static HttpResponse login(String requestUrl, String username, String password) throws IOException {
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, null);
        HttpConnector httpConnector = new HttpConnector();
        String url = "http://" + requestUrl + "/xwiki/bin/login/XWiki/XWikiLogin";
        HttpRequest request = new HttpRequest(url);
        String basicAuth = username + ":" + password;
        basicAuth = "Basic " + new String(Base64.encodeToString(basicAuth.getBytes(), Base64.NO_WRAP));
        request.httpParams.putHeaders("Authorization", basicAuth);
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            return response;
            //throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        //global value setting for http
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, response.getHeaders().get("Set-Cookie"));
        return response;
    }


    public static String signUpInitCookieForm() throws IOException {
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, null);
        String registerUrl = "http://"+ getServerAddress()+"/xwiki/bin/view/XWiki/Registration";
        if(registerUrl.contains("www.xwiki.org")){
            registerUrl = "http://"+ getServerAddress()+"/xwiki/bin/view/XWiki/RealRegistration";
        }
        HttpRequest request = new HttpRequest(registerUrl);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            return null;
        }
        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, response.getHeaders().get("Set-Cookie"));
        byte[] contentData = response.getContentData();
        Document document = Jsoup.parse(new String(contentData));
        String formToken = document.select("input[name=form_token]").val();
        return formToken;
    }

    /**
     * sign Up
     * @param userId
     * @param password
     * @param formToken
     * @param captcha
     * @param firstName
     * @param lastName
     * @param email
     * @return
     * @throws IOException
     * //String registerUrl = "http://210.76.192.253:8080/xwiki/bin/view/XWiki/Registration";
     */
    public static Boolean signUp(String userId, String password, String formToken, String captcha, String firstName, String lastName, String email) throws IOException {
        String registerUrl = "http://"+ getServerAddress()+"/xwiki/bin/view/XWiki/Registration";
        if(registerUrl.contains("www.xwiki.org")){
            registerUrl = "http://"+ getServerAddress()+"/xwiki/bin/view/XWiki/RealRegistration";
        }
        HttpRequest request = new HttpRequest(registerUrl, HttpRequest.HttpMethod.POST, null);
        HttpConnector httpConnector = new HttpConnector();
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
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        byte[] contentData = response.getContentData();
        Document document = Jsoup.parse(new String(contentData));
        formToken = document.select("input[name=template]").val();
        if(TextUtils.isEmpty(formToken)){
            return true;
        }
        return false;
    }

    /**
     * sign up
     * @param userId
     * @param password
     * @param formToken
     * @return
     */
    public static Boolean signUp(String userId, String password, String formToken, String captcha) throws IOException {
        return signUp(userId, password, formToken, captcha, "", "", "");
    }

    /**
     * get All user Map <serverId, SearchResult> for getting deleting users
     * that have been delete.
     * @param wiki
     * @param number
     * @return HashMap<String, SearchResult>
     * @throws IOException
     */
    private static HashMap<String, SearchResult> getAllUserMap(String wiki, int number) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        HashMap<String, SearchResult> maps = new HashMap<>();
        for(SearchResult item : searchlist){
            maps.put(item.id, item);
        }
        return maps;
    }

    /**
     * getUserList
     * get wiki Users  total<=number
     * @param wiki
     * @param number
     * @return List<XWikiUser>
     * @throws IOException
     * http://xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=10
     */
    private static List<XWikiUser> getUserList(String wiki, int number) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        for(SearchResult item : searchlist){
            XWikiUser user = getUserDetail(item.id);
            user.lastModifiedDate = item.modified;
            userList.add(user);
        }
        return userList;
    }

    /**
     * Get wiki users modified from lastSyncTime
     * @param wiki
     * @param number
     * @param lastSyncTime
     * @return List<XWikiUser>
     * @throws IOException
     */
    private static List<XWikiUser> getUserList(String wiki, int number, String lastSyncTime) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = null;
        for(SearchResult item : searchlist){
            itemDate = StringUtils.iso8601ToDate(item.modified);
            if(itemDate.before(lastSynDate)) continue;
            XWikiUser user = getUserDetail(item.id);
            user.lastModifiedDate = item.modified;
            userList.add(user);
        }
        return userList;
    }


    /**
     * get user information
     * @param id  curriki:XWiki.Luisafan
     * @return XWikiUser  Without Getting LastModifiedDate
     * @throws IOException
     */
    public static XWikiUser getUserDetail(String id) throws IOException, XmlPullParserException {
        String[] split = XWikiUser.splitId(id);
        if(split == null) throw new IOException(TAG+ ",in getUserDetail, userId error");
        return getUserDetail(split[0], split[1], split[2]);
    }





    /**
     * get user information
     * @param wiki
     * @param space
     * @param name
     * @return XWikiUser  Without Getting LastModifiedDate
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static XWikiUser getUserDetail(String wiki, String space, String name) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/"+ wiki +"/spaces/"+ space +"/pages/"+ name +"/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if(statusCode == 404){
            return null;
        }
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        XWikiUser user = XmlUtils.getXWikiUser(new ByteArrayInputStream(response.getContentData()));
        return user;
    }

    /**
     * update user information
     * @param user
     * @return true:update success, false:update fail
     * @throws IOException
     * curl -u fitz:fitz2xwiki -X PUT -H "Content-type: application/x-www-form-urlencoded" -d "className=XWiki.XWikiUsers" -d "property#company=iiedacas" http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/fitz/objects/XWiki.XWikiUsers/0
     */
    public static boolean updateUser(XWikiUser user) throws IOException{
        String url = getServerRestUrl() + "/wikis/"+user.wiki+"/spaces/"+user.space+"/pages/"+user.pageName+"/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.PUT, null);
        request.httpParams.putBodyParams("className", "XWiki.XWikiUsers");
        request.httpParams.putBodyParams("property#first_name", user.firstName);
        request.httpParams.putBodyParams("property#last_name", user.lastName);
        request.httpParams.putBodyParams("property#email", user.email);
        request.httpParams.putBodyParams("property#phone", user.phone);
        //request.httpParams.putHeaders("company", user.company);
        //request.httpParams.putHeaders("blog", user.blog);
        //request.httpParams.putHeaders("blogfeed", user.blogFeed);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            //throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
            return false;
        }
        return true;
    }

    /**
     * get all groups
     * @param number
     * @return List<XWikiGroup>
     * @throws IOException
     * http://www.xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiGroups&number=20
     */
    public static List<XWikiGroup> getGroupList(int number) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/query?q=" + "object:XWiki.XWikiGroups&number="+ number;
        //String wiki,  wiki:"+ wiki
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiGroup> groupList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        for(SearchResult item : searchlist){
            XWikiGroup group = new XWikiGroup();
            group.id = item.id;
            group.wiki = item.wiki;
            group.space = item.space;
            group.pageName = item.pageName;
            group.lastModifiedDate = item.modified;
            group.version = item.version;
            groupList.add(group);
        }
        return groupList;
    }

    /**
     * get users from the group, total<=number
     * @param groupId
     * @param number
     * @return LIst<XWikiUser>
     * @throws IOException
     * http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects
     */
    private static List<XWikiUser> getUserListFromGroup(String groupId, int number) throws IOException, XmlPullParserException {
        String[] split = XWikiUser.splitId(groupId);
        if(split == null) throw new IOException(TAG+ ",in getUserListFromGroup, groupId error");
        String url = getServerRestUrl() + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2] +"/objects/XWiki.XWikiGroups";
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<ObjectSummary> objectlist = XmlUtils.getObjectSummarys(new ByteArrayInputStream(response.getContentData()));
        for(ObjectSummary item : objectlist){
            XWikiUser user = getUserDetail(item.headline);
            userList.add(user);
        }
        return userList;
    }

    public static class SyncData{
        List<XWikiUser> updateUserList;
        HashSet<String> allIdSet;

        public SyncData(){
            updateUserList = new ArrayList<>();
            allIdSet = new HashSet<>();
        }

        public List<XWikiUser> getUpdateUserList() {
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

    public static SyncData getSyncData(String lastSyncTime, int syncType) throws IOException, XmlPullParserException {
        if(syncType == Constants.SYNC_TYPE_ALL_USERS){
            return getSyncAllUsers(lastSyncTime);
        }else if(syncType == Constants.SYNC_TYPE_SELECTED_GROUPS){
            List<String> groupIdList = SharedPrefsUtils.getArrayList(AppContext.getInstance().getApplicationContext(), Constants.SELECTED_GROUPS);
            return getSyncGroups(groupIdList, lastSyncTime);
        }
        throw  new IOException(TAG+ "syncType error, SyncType="+syncType);
    }

    private static SyncData getSyncGroups(List<String> groupIdList, String lastSyncTime) throws IOException, XmlPullParserException {
        if(groupIdList == null || groupIdList.size()==0) return null;
        SyncData syncData = new SyncData();
        for(String groupId : groupIdList) {
            String[] split = XWikiUser.splitId(groupId);
            if (split == null) throw new IOException(TAG+ ",in getSyncGroups, groupId error");
            String url = getServerRestUrl() + "/wikis/" + split[0] + "/spaces/" + split[1] + "/pages/" + split[2] + "/objects/XWiki.XWikiGroups";
            HttpRequest request = new HttpRequest(url);
            HttpConnector httpConnector = new HttpConnector();
            HttpResponse response = httpConnector.performRequest(request);
            int statusCode = response.getResponseCode();
            if (statusCode < 200 || statusCode > 299) {
                throw new IOException("statusCode=" + statusCode + ",response=" + response.getResponseMessage());
            }
            Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
            Date itemDate = null;
            List<ObjectSummary> objectList = XmlUtils.getObjectSummarys(new ByteArrayInputStream(response.getContentData()));
            for (ObjectSummary item : objectList) {
                //TODO ask why this situation occur? <headline>xwiki:XWiki.gdelhumeau</headline>
                if(item.headline.startsWith(split[0])){
                    item.headline = item.headline.substring(split[0].length()+1);
                }
                syncData.allIdSet.add(split[0]+":"+item.headline);
                itemDate = getUserLastModified(split[0], item.headline);
                if (itemDate == null || itemDate.before(lastSynDate)) continue;
                String[] spaceAndName = item.headline.split("\\.");
                XWikiUser user = getUserDetail(split[0], spaceAndName[0], spaceAndName[1]);
                syncData.updateUserList.add(user);
            }
        }
        return syncData;
    }

    //TODO Comment null,size=0: no data need be updated.
    //exception is getData Exception. so the lastSyncTime should not be set to new Date() because of sync error.
    //NOTE if the network connection is lost in the update and we don't throw exception here and just continue,
    // how to ensure that all users are added and updated in local database from lastSyncTime because lastSyncTime is modified to new Date
    //but the modified data may be bypass and not be updated or added successfully.
    // if we throw exception here, it's ok! but if not, the user lost in the network exception will never be stored in local db.
    // Solution 1: when traversing users of local database, in delete-sync process, we should delete the id that has been traversed from set,
    // All that's left is the users we should Add again in local db from server.  even then, this solution is also a failure because we can not find the data
    //that should be updated from lastSyncTime but not be updated because of network continue.
    //Solution 2: Cache may help a lot. And this is the best way.
    private static SyncData getSyncAllUsers(String lastSyncTime) throws IOException, XmlPullParserException {
        String url = getServerRestUrl() + "/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number="+ Constants.LIMIT_MAX_SYNC_USERS;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<SearchResult> searchList = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        SyncData syncData = new SyncData();
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = null;
        for(SearchResult item : searchList){
            syncData.allIdSet.add(item.id);
            itemDate = StringUtils.iso8601ToDate(item.modified);
            if(itemDate.before(lastSynDate)) continue;
            XWikiUser user = getUserDetail(item.id);
            user.lastModifiedDate = item.modified;
            syncData.updateUserList.add(user);
        }
        return syncData;
    }


    /**
     * get all users modified from lastSyncTime in the group
     * @param groupId
     * @param number
     * @param lastSyncTime
     * @return List<XWikiUser>
     * @throws IOException
     * http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects/XWiki.XWikiGroups
     */
    private static List<XWikiUser> getUserListFromGroup(String groupId, int number, String lastSyncTime) throws IOException, XmlPullParserException {
        //TODO Maybe have another way !
        String[] split = XWikiUser.splitId(groupId);
        if(split == null) throw new IOException(TAG+ ",in getUserListFromGroup, groupId error");
        String url = getServerRestUrl() + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2] +"/objects/XWiki.XWikiGroups";
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = null;
        List<XWikiUser> userList = new ArrayList<>();
        List<ObjectSummary> objectList = XmlUtils.getObjectSummarys(new ByteArrayInputStream(response.getContentData()));
        for(ObjectSummary item : objectList){
            itemDate = getUserLastModified(split[0], item.headline);
            if(itemDate == null || itemDate.before(lastSynDate)) continue;
            XWikiUser user = getUserDetail(item.headline);
            userList.add(user);
        }
        return userList;
    }


    /**
     * get User Lass Modified Time
     * @param id
     * @return Date
     * @throws IOException
     * http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/zhouwenhai
     * http://www.xwiki.org/xwiki/rest/wikis/query?q=object:XWiki.XWikiUsers%20and%20name:fitz
     */
    private static Date getUserLastModified(String wiki, String id) throws IOException, XmlPullParserException {
        //TODO getUserLastModified. Maybe have another better way to query lastModifiedDate
        String[] split = id.split("\\.");
        if(split == null) throw new IOException(TAG+ ",in getUserLastModified, groupId error" + id);
        String url = getServerRestUrl() + "/wikis/" + wiki + "/spaces/" + split[0] +"/pages/" + split[1];
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        // 404 Not Found return null; because the user may not exist.
        if(statusCode == 404){
            return null;
        }
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        Page page = XmlUtils.getPage(new ByteArrayInputStream(response.getContentData()));
        Date date = StringUtils.iso8601ToDate(page.lastModified);
        return date;
    }

    /**
     * get serverRestPreUrl from preference.
     * @return url
     */
    public static String getServerRestUrl(){
        if(serverRestPreUrl == null) {
            serverRestPreUrl = getServerAddress();
            serverRestPreUrl = "http://" + serverRestPreUrl + "/xwiki/rest";
        }
        return serverRestPreUrl;
    }

    public static String getServerAddress(){
        String requestUrl = SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(), Constants.SERVER_ADDRESS, null);
        return requestUrl;
    }

    public static void setRestUrlNULL(){
        serverRestPreUrl = null;
    }


    /**
     *
     * @param user
     * @param avatarName
     * @return
     * http://www.xwiki.org/xwiki/bin/download/XWiki/jvelociter/jvelociter.jpg
     */
    public static byte[] downloadAvatar(String user, String avatarName){
        String url = "http://www.xwiki.org/xwiki/bin/download/XWiki/"+user+"/"+avatarName;
        return downloadAvatar(url);
    }


    /**
     * Download the avatar image from the server.
     *
     * @param avatarUrl the URL pointing to the avatar image
     * @return a byte array with the raw JPEG avatar image
     */
    public static byte[] downloadAvatar(final String avatarUrl) {
        // If there is no avatar, we're done
        if (TextUtils.isEmpty(avatarUrl)) {
            return null;
        }

        try {
            Log.i(TAG, "Downloading avatar: " + avatarUrl);
            // Request the avatar image from the server, and create a bitmap
            // object from the stream we get back.
            //URL url = new URL(avatarUrl);
            //HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();
            HttpRequest request = new HttpRequest(avatarUrl);
            HttpConnector httpConnector = new HttpConnector();
            HttpResponse response = httpConnector.performRequest(request);
            //int statusCode = response.getResponseCode();
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap avatar = BitmapFactory.decodeStream(new ByteArrayInputStream(response.getContentData()),
                        null, options);
                avatar = ImageUtils.compressByQuality(avatar, 900);

                // Take the image we received from the server, whatever format it
                // happens to be in, and convert it to a JPEG image. Note: we're
                // not resizing the avatar - we assume that the image we get from
                // the server is a reasonable size...
                Log.i(TAG, "Converting avatar to JPEG");
                if(avatar == null) return null;
                ByteArrayOutputStream convertStream = new ByteArrayOutputStream(
                        avatar.getWidth() * avatar.getHeight() * 4);
                avatar.compress(Bitmap.CompressFormat.JPEG, 95, convertStream);
                convertStream.flush();
                convertStream.close();
                // On pre-Honeycomb systems, it's important to call recycle on bitmaps
                avatar.recycle();
                return convertStream.toByteArray();
            } finally {
                //connection.disconnect();
            }
        } catch (MalformedURLException muex) {
            // A bad URL - nothing we can really do about it here...
            Log.e(TAG, "Malformed avatar URL: " + avatarUrl);
        } catch (IOException ioex) {
            // If we're unable to download the avatar, it's a bummer but not the
            // end of the world. We'll try to get it next time we sync.
            Log.e(TAG, "Failed to download user avatar: " + avatarUrl);
        }
        return null;
    }

}
