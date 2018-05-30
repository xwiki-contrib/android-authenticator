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
import android.util.Log;

import org.xwiki.android.authenticator.rest.BaseApiManager;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AppContext.
 */
public class AppContext extends Application {
    private static Map.Entry<String, BaseApiManager> baseApiManager;
    private static final String TAG = "AppContext";

    private static AppContext instance;

    public static AppContext getInstance() {
        return instance;
    }

    public static String currentBaseUrl() {
        return SharedPrefsUtils.getValue(instance, Constants.SERVER_ADDRESS, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "on create");
    }

    public static void addAuthorizedApp(int uid, String packageName) {
        Log.d(TAG, "packageName=" + packageName + ", uid=" + uid);
        List<String> packageList = SharedPrefsUtils.getArrayList(instance.getApplicationContext(), Constants.PACKAGE_LIST);
        if (packageList == null) {
            packageList = new ArrayList<>();
        }
        packageList.add(packageName);
        SharedPrefsUtils.putArrayList(instance.getApplicationContext(), Constants.PACKAGE_LIST, packageList);
    }

    public static boolean isAuthorizedApp(String packageName) {
        List<String> packageList = SharedPrefsUtils.getArrayList(instance.getApplicationContext(), Constants.PACKAGE_LIST);
        if (packageList != null && packageList.contains(packageName)) {
            return true;
        }
        return false;
    }

    public static BaseApiManager getApiManager() {
        String url = currentBaseUrl();
        if (baseApiManager == null || !baseApiManager.getKey().equals(url)) {
            baseApiManager = new AbstractMap.SimpleEntry<>(
                url,
                new BaseApiManager(url)
            );
        }
        return baseApiManager.getValue();
    }
}
