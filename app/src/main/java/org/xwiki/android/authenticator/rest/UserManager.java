package org.xwiki.android.authenticator.rest;

import android.os.Handler;

import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiUsers;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iof on 2016/4/20.
 */
public class UserManager {
    public interface Callback{
        void onResponse(List<XWikiUsers> usersList);
    }

    public static void getAllUser(final String url, final Callback callback){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<XWikiUsers> usersList = new ArrayList<XWikiUsers>();
                final String response = NetUtils.get(url);
                if(response==null) return;
                List<SearchResult> list = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getBytes()));
                for(SearchResult result:list){
                    String userUrl = "http://xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/"+result.pageName+"/objects/XWiki.XWikiUsers/0";
                    String userInfoResponse = NetUtils.get(userUrl);
                    if(userInfoResponse==null) continue;
                    XWikiUsers user = XmlUtils.getXWikiUsers(new ByteArrayInputStream(userInfoResponse.getBytes()));
                    usersList.add(user);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(usersList);
                    }
                });
            }
        }).start();
    }



}
