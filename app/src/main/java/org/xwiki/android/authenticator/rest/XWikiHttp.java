package org.xwiki.android.authenticator.rest;

import android.util.Base64;

import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.Page;
import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fitz on 2016/4/25.
 */
public class XWikiHttp {
    //http://xwiki.org/xwiki/rest
    private final String serverRestPreUrl = "http://xwikichina.com/xwiki/rest";

    /**
     * login
     * @param username
     * @param password
     * @return Body byte[]
     * @throws IOException
     */
    public byte[] login(String username, String password) throws IOException {
        HttpConnector httpConnector = new HttpConnector();
        String url = serverRestPreUrl;
        HttpRequest request = new HttpRequest(url);
        String basicAuth = username + ":" + password;
        basicAuth = "Basic " + new String(Base64.encodeToString(basicAuth.getBytes(), Base64.NO_WRAP));
        request.httpParams.putHeaders("Authorization", basicAuth);
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        return response.getContentData();
    }

    /**
     * getUserList
     * get wiki Users  total<=number
     * @param wiki
     * @param number
     * @return List<XWikiUser>
     * @throws IOException
     */
    //http://xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=10
    public List<XWikiUser> getUserList(String wiki, int number) throws IOException{
        String url = serverRestPreUrl + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        if(searchlist == null) return null;
        for(SearchResult item : searchlist){
            XWikiUser user = getUserDetail(item.pageName);
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
    public List<XWikiUser> getUserList(String wiki, int number, String lastSyncTime) throws IOException{
        String url = serverRestPreUrl + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUser> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        if(searchlist == null) return null;
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = new Date();
        for(SearchResult item : searchlist){
            itemDate = StringUtils.iso8601ToDate(item.modified);
            if(itemDate.before(lastSynDate)) continue;
            XWikiUser user = getUserDetail(item.pageName);
            userList.add(user);
        }
        return userList;
    }

    /**
     * get user information
     * @param username
     * @return XWikiUser
     * @throws IOException
     */
    public XWikiUser getUserDetail(String username) throws IOException{
        String url = serverRestPreUrl + "/wikis/xwiki/spaces/XWiki/pages/"+username+"/objects/XWiki.XWikiUsers/0";
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
     */
    public Boolean updateUser(XWikiUser user) throws IOException{
        String url = serverRestPreUrl + "/wikis/xwiki/spaces/XWiki/pages/"+user.id+"/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.PUT, null);
        request.httpParams.putHeaders("email",user.email);
//        request.httpParams.putHeaders("company",);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        return true;
    }

    /**
     * get all groups
     * @param wiki
     * @param number
     * @return List<XWikiGroup>
     * @throws IOException
     */
    //http://www.xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiGroups&number=20
    public List<XWikiGroup> getGroupList(String wiki, int number) throws IOException{
        String url = serverRestPreUrl + "/wikis/query?q=wiki:"+ wiki +
                "%20and%20object:XWiki.XWikiGroups&number="+ number;
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
            group.id = item.pageName;
            group.name = item.pageFullName;
            group.date = item.modified;
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
     */
    //http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects
    public List<XWikiUser> getUserListFromGroup(String groupId, int number) throws IOException{
        String url = serverRestPreUrl + "/wikis/xwiki/spaces/XWiki/pages/"+ groupId +"/objects";
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
     */
    public List<XWikiUser> getUserListFromGroup(String groupId, int number, String lastSyncTime) throws IOException{
        String url = serverRestPreUrl + "/wikis/xwiki/spaces/XWiki/pages/"+ groupId +"/objects";
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        Date lastSynDate = StringUtils.iso8601ToDate(lastSyncTime);
        Date itemDate = new Date();
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
     * @param username
     * @return Date
     * @throws IOException
     */
    //http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/zhouwenhai
    private Date getUserLastModified(String username) throws IOException{
        //TODO getUserLastModified
        String url = serverRestPreUrl + "/wikis/xwiki/spaces/XWiki/pages/" + username;
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




}
