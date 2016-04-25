package org.xwiki.android.authenticator.rest;


import android.widget.TextView;

import org.w3c.dom.Text;
import org.xwiki.android.authenticator.bean.XWikiUser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by fitz on 2016/4/25.
 */
public class Test {

    public static void testLogin(final HttpCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] result = new XWikiHttp().login("fitz", "fitz2xwiki");
                    callback.postSuccess(result);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.postFailure(e.getMessage());
                }
            }
        }).start();
    }

    public static void testGetAllUser(final HttpCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<XWikiUser> userList = new XWikiHttp().getUserList("xwiki",10);
                    callback.postSuccess(userList);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.postFailure(e.getMessage());
                }
            }
        }).start();
    }


    public static void testDate(final TextView textView){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        textView.append(sdf.format(new Date()));
        try {
            Date date = sdf.parse("2011-09-24T19:45:31+02:00");
            textView.append(date.toGMTString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
