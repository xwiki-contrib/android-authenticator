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
package org.xwiki.android.sync.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utils for shared preferences
 *
 * @version $Id$
 */
public class SharedPrefsUtils {

    /**
     * Settings shared preferences name.
     */
    private final static String SETTING = "Setting";

    /**
     * Put value into shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key to add
     * @param value Value to add
     */
    public static void putValue(Context context, String key, int value) {
        Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        sp.putInt(key, value);
        sp.apply();
    }

    /**
     * Put value into shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key to add
     * @param value Value to add
     */
    public static void putValue(Context context, String key, boolean value) {
        Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.apply();
    }

    /**
     * Put value into shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key to add
     * @param value Value to add
     */
    public static void putValue(Context context, String key, String value) {
        Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.apply();
    }

    /**
     * Get value from shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key for getting
     * @param defValue Value which will be returned if value by key is absent
     * @return int value if exists or defValue
     */
    public static int getValue(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

    /**
     * Get value from shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key for getting
     * @param defValue Value which will be returned if value by key is absent
     * @return boolean value if exists or defValue
     */
    public static boolean getValue(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    /**
     * Get value from shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key for getting
     * @param defValue Value which will be returned if value by key is absent
     * @return String value if exists or defValue
     */
    public static String getValue(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    /**
     * Put value into shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key to add
     * @param list Value to add
     */
    public static void putArrayList(Context context, String key, List<String> list) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        Set<String> set = new HashSet<>(list);
        edit.putStringSet(key, set);
        edit.apply();
    }

    /**
     * Get value from shared preferences.
     *
     * @param context Context for getting shared preferences
     * @param key Key for getting
     * @return List value if exists or null
     */
    public static List<String> getArrayList(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        Set<String> set = sp.getStringSet(key, null);
        if (set == null) return null;
        return new ArrayList<>(set);
    }

    /**
     * Clear all data from shared preferences.
     *
     * @param context Context for getting shared preferences
     */
    public static void clearAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    /**
     * Remove value from shared preferences by key.
     *
     * @param context Context for getting shared preferences
     * @param key Key for removing
     */
    public static void removeKeyValue(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        sp.edit().remove(key).apply();
    }

}

