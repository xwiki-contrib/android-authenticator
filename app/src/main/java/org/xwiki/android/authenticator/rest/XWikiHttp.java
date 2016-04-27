package org.xwiki.android.authenticator.rest;

import android.util.Base64;

import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.Page;
import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiGroup;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.utils.Loger;
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
    private final String serverRestPreUrl = "http://xwikichina.com/xwiki/rest";
//    private final String serverRestPreUrl = "http://210.76.195.251:8080/xwiki/rest";

    /**
     * login
     * @param username
     * @param password
     * @return Body byte[]
     * @throws IOException
     */
    public HttpResponse login(String username, String password) throws IOException {
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
    public Boolean signUp(String userId, String password, String firstName, String lastName, String email){

        return true;
    }


    public HashMap<String, SearchResult> getAllUserMap(String wiki, int number) throws IOException{
        String url = serverRestPreUrl + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
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
     * //http://xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=10
     */
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
        Date itemDate = null;
        for(SearchResult item : searchlist){
            itemDate = StringUtils.iso8601ToDate(item.modified);
            Loger.debug(item.toString());
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
    public XWikiUser getUserDetail(String id) throws IOException{
        String[] split = XWikiUser.splitId(id);
        if(split == null) return null;
        String url = serverRestPreUrl + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2]+"/objects/XWiki.XWikiUsers/0";
        Loger.debug(url);
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
        String url = serverRestPreUrl + "/wikis/+"+user.wiki+"+/spaces/"+user.space+"/pages/"+user.pageName+"/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.PUT, null);
        request.httpParams.putHeaders("first_name", user.firstName);
        request.httpParams.putHeaders("last_name", user.lastName);
        request.httpParams.putHeaders("email", user.email);
        request.httpParams.putHeaders("company", user.company);
        request.httpParams.putHeaders("blog", user.blog);
        request.httpParams.putHeaders("blogfeed", user.blogFeed);
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
     * //http://www.xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiGroups&number=20
     */
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
            group.id = item.id;
            group.wiki = item.wiki;
            group.space = item.space;
            group.pageName = item.pageName;
            group.lastModifiedDate = item.modified;
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
     * //http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects
     */
    public List<XWikiUser> getUserListFromGroup(String groupId, int number) throws IOException{
        String[] split = XWikiUser.splitId(groupId);
        if(split == null) return null;
        String url = serverRestPreUrl + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2] +"/objects/XWiki.XWikiGroups";
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
    public List<XWikiUser> getUserListFromGroup(String groupId, int number, String lastSyncTime) throws IOException{
        //TODO Maybe have another way !
        String[] split = XWikiUser.splitId(groupId);
        if(split == null) return null;
        String url = serverRestPreUrl + "/wikis/"+split[0]+"/spaces/"+split[1]+"/pages/"+split[2] +"/objects/XWiki.XWikiGroups";
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
     * @param
     * @return Date
     * @throws IOException
     * //http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/zhouwenhai
     * //http://www.xwiki.org/xwiki/rest/wikis/query?q=object:XWiki.XWikiUsers%20and%20name:fitz
     */
    private Date getUserLastModified(String id) throws IOException{
        //TODO getUserLastModified. Maybe have another better way to query lastModifiedDate
        String[] split = XWikiUser.splitId(id);
        if(split == null) return null;
        String url = serverRestPreUrl + "/wikis/" + split[0] + "/spaces/" + split[1] +"/pages/" + split[2];
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
