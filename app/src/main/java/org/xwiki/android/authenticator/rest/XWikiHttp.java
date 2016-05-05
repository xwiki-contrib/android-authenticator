package org.xwiki.android.authenticator.rest;

import android.util.Base64;

import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.Page;
import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.utils.SharedPrefsUtil;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fitz on 2016/4/25.
 */
public class XWikiHttp {
    //http://xwiki.org/xwiki/rest
    //private static String serverRestPreUrl = "http://xwikichina.com/xwiki/rest"
    private static String serverRestPreUrl = null; //"http://210.76.192.145:8080/xwiki/rest";
    //private static String serverRestPreUrl = "http://192.168.201.2:8080/xwiki/rest";


    /**
     * login
     * @param username
     * @param password
     * @return Body byte[]
     * @throws IOException
     * http://localhost:8080/xwiki/bin/login/XWiki/XWikiLogin
     */
    public static HttpResponse login(String requestUrl, String username, String password) throws IOException {
        SharedPrefsUtil.putValue(AppContext.getInstance().getApplicationContext(), "Cookie", null);
        HttpConnector httpConnector = new HttpConnector();
        String url = "http://" + requestUrl + "/xwiki/bin/login/XWiki/XWikiLogin";
        serverRestPreUrl = url;
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
        SharedPrefsUtil.putValue(AppContext.getInstance().getApplicationContext(), "Cookie", response.getHeaders().get("Set-Cookie"));
        serverRestPreUrl = "http://" + requestUrl + "/xwiki/rest";
        SharedPrefsUtil.putValue(AppContext.getInstance().getApplicationContext(), "requestUrl", requestUrl);
        SharedPrefsUtil.putValue(AppContext.getInstance().getApplicationContext(), "ServerUrl", serverRestPreUrl);
        return response;
    }

    /**
     * sign up  //TODO signUp
     * @param userId
     * @param password
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    public static Boolean signUp(String userId, String password, String firstName, String lastName, String email){

        return true;
    }

    /**
     * get All user Map <serverId, SearchResult> for getting deleting users
     * that have been delete.
     * @param wiki
     * @param number
     * @return HashMap<String, SearchResult>
     * @throws IOException
     */
    public static HashMap<String, SearchResult> getAllUserMap(String wiki, int number) throws IOException{
        String url = getServerUrl() + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        if(searchlist == null) return null;
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
    public static List<XWikiUser> getUserList(String wiki, int number) throws IOException{
        String url = getServerUrl() + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        if(searchlist == null) return null;
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
    public static List<XWikiUser> getUserList(String wiki, int number, String lastSyncTime) throws IOException{
        String url = getServerUrl() + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        if(searchlist == null) return null;
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = null;
        for(SearchResult item : searchlist){
            itemDate = StringUtils.iso8601ToDate(item.modified);
            if(itemDate.before(lastSynDate)) continue;
            XWikiUser user = getUserDetail(item.id);
            if(user == null) continue;
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
    public static XWikiUser getUserDetail(String id) throws IOException{
        String[] split = XWikiUser.splitId(id);
        if(split == null) return null;
        String url = getServerUrl() + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2]+"/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
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
        String url = getServerUrl() + "/wikis/"+user.wiki+"/spaces/"+user.space+"/pages/"+user.pageName+"/objects/XWiki.XWikiUsers/0";
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
    public static List<XWikiGroup> getGroupList(int number) throws IOException{
        String url = getServerUrl() + "/wikis/query?q=" +
                "object:XWiki.XWikiGroups&number="+ number;
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
        if(searchlist == null) return null;
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
    public static List<XWikiUser> getUserListFromGroup(String groupId, int number) throws IOException{
        String[] split = XWikiUser.splitId(groupId);
        if(split == null) return null;
        String url = getServerUrl() + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2] +"/objects/XWiki.XWikiGroups";
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<ObjectSummary> objectlist = XmlUtils.getObjectSummarys(new ByteArrayInputStream(response.getContentData()));
        if(objectlist == null) return null;
        for(ObjectSummary item : objectlist){
            XWikiUser user = getUserDetail(item.headline.substring(6));
            userList.add(user);
        }
        return userList;
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
    public static List<XWikiUser> getUserListFromGroup(String groupId, int number, String lastSyncTime) throws IOException{
        //TODO Maybe have another way !
        String[] split = XWikiUser.splitId(groupId);
        if(split == null) return null;
        String url = getServerUrl() + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2] +"/objects/XWiki.XWikiGroups";
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
        List<ObjectSummary> objectlist = XmlUtils.getObjectSummarys(new ByteArrayInputStream(response.getContentData()));
        if(objectlist == null) return null;
        for(ObjectSummary item : objectlist){
            itemDate = getUserLastModified(item.headline.substring(6));
            if(itemDate.before(lastSynDate)) continue;
            XWikiUser user = getUserDetail(item.headline.substring(6));
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
    private static Date getUserLastModified(String id) throws IOException{
        //TODO getUserLastModified. Maybe have another better way to query lastModifiedDate
        String[] split = XWikiUser.splitId(id);
        if(split == null) return null;
        String url = getServerUrl() + "/wikis/" + split[0] + "/spaces/" + split[1] +"/pages/" + split[2];
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        Page page = XmlUtils.getPage(new ByteArrayInputStream(response.getContentData()));
        if(page == null) return null;
        Date date = StringUtils.iso8601ToDate(page.lastModified);
        return date;
    }

    /**
     * get serverRestPreUrl from preference.
     * Maybe don't need because just update in the sign-in activity .
     * @return url
     */
    public static String getServerUrl(){
        if(serverRestPreUrl == null) {
            serverRestPreUrl = SharedPrefsUtil.getValue(AppContext.getInstance().getApplicationContext(), "ServerUrl", null);
        };
        return serverRestPreUrl;
    }

}
