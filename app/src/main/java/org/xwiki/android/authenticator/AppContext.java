package org.xwiki.android.authenticator;

import android.app.Application;
import android.util.Log;

import org.xwiki.android.authenticator.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fitz on 2016/4/30.
 */
public class AppContext extends Application{
    private static final String TAG = "AppContext";

    private static AppContext instance;

    public static AppContext getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "on create");
    }

    public static void addAuthorizedApp(int uid, String packageName){
        Log.d(TAG, "packageName="+packageName+", uid="+uid);
        SharedPrefsUtil.putValue(instance.getApplicationContext(), "appuid"+uid, packageName);
        List<String> packageList = SharedPrefsUtil.getArrayList(instance.getApplicationContext(), "packageList");
        if(packageList == null){
            packageList = new ArrayList<>();
        }
        packageList.add(packageName);
        SharedPrefsUtil.putArrayList(instance.getApplicationContext(), "packageList", packageList);
    }

    public static boolean isAuthorizedApp(int uid){
        String packageName = SharedPrefsUtil.getValue(instance.getApplicationContext(), "appuid"+uid, null);
        if(packageName == null){
            return false;
        }
        return true;
    }
}
