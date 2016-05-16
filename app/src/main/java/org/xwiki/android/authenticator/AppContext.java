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
package org.xwiki.android.authenticator;

import android.app.Application;
import android.provider.ContactsContract;
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
        SharedPrefsUtil.putValue(instance.getApplicationContext(), Constants.APP_UID + uid, packageName);
        List<String> packageList = SharedPrefsUtil.getArrayList(instance.getApplicationContext(), Constants.PACKAGE_LIST);
        if(packageList == null){
            packageList = new ArrayList<>();
        }
        packageList.add(packageName);
        SharedPrefsUtil.putArrayList(instance.getApplicationContext(), Constants.PACKAGE_LIST, packageList);
    }

    public static boolean isAuthorizedApp(int uid){
        String packageName = SharedPrefsUtil.getValue(instance.getApplicationContext(), Constants.APP_UID + uid, null);
        if(packageName == null){
            return false;
        }
        return true;
    }
}
