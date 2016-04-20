package org.xwiki.android.authenticator.rest;

import android.widget.TextView;

import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiUsers;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Created by fitz on 2016/4/20.
 */
public class RestTest {
    public static void testGetAllUsers(final TextView textView){
        String requestUrl = "http://xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=20";
        UserManager.getAllUser(requestUrl, new UserManager.Callback() {
            @Override
            public void onResponse(List<XWikiUsers> usersList) {
                textView.setText(usersList.toString());
            }
        });
    }

    public static void testGetSearchResult(final TextView textView){
        String requestUrl = "http://xwikichina.com/xwiki/rest/wikis/query?q=object:XWiki.XWikiUsers&number=20";
        AsynNetUtils.get(requestUrl, new AsynNetUtils.Callback() {
            @Override
            public void onResponse(String response) {
                textView.setText(response);
                List<SearchResult> list = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getBytes()));
                textView.append(list.toString());
            }
        });
    }

    public static void testGetUserInfo(final TextView textView){
        String requestUrl = "http://xwikichina.com/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/fitz/objects/XWiki.XWikiUsers/0";
        AsynNetUtils.get(requestUrl, new AsynNetUtils.Callback() {
            @Override
            public void onResponse(String response) {
                textView.setText(response);
                XWikiUsers user = XmlUtils.getXWikiUsers(new ByteArrayInputStream(response.getBytes()));
                textView.setText(user.toString());
            }
        });
    }

    void apiTest(){
        //sql query  getUsersModified getUsersModifiedFromOneGroup getAllGroups
        // xml -> searchResult -> objects -> objects detail　-> store db

        //modify or edit object ->  xml -> requestUpdate put post -> server
        //updateUser deleteUser
        //
    }
}
