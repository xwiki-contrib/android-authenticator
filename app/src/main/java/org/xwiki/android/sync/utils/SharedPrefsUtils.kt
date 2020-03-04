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
package org.xwiki.android.sync.utils

import android.content.Context
import java.util.*


/**
 * Settings shared preferences name.
 */

private val SETTING = "Setting"

/**
 * Put value into shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key to add
 * @param value Value to add
 */
fun putValue(context: Context, key: String, value: Int) {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit()
    sp.putInt(key, value)
    sp.apply()
}

/**
 * Put value into shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key to add
 * @param value Value to add
 */
fun putValue(context: Context, key: String, value: Boolean) {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit()
    sp.putBoolean(key, value)
    sp.apply()
}

/**
 * Put value into shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key to add
 * @param value Value to add
 */
fun putValue(context: Context, key: String, value: String) {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit()
    sp.putString(key, value)
    sp.apply()
}

/**
 * Put value into shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key to add
 * @param list Value to add
 */
fun putArrayList(context: Context, key: String, list: List<String>) {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
    val edit = sp.edit()
    val set = HashSet(list)
    edit.putStringSet(key, set)
    edit.apply()
}

/**
 * Get value from shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key for getting
 * @return List value if exists or null
 */
fun getArrayList(context: Context, key: String): MutableList<String>? {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
    val set = sp.getStringSet(key, null) ?: return null
    return ArrayList(set)
}

/**
 * Clear all data from shared preferences.
 *
 * @param context Context for getting shared preferences
 */
fun clearAll(context: Context) {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
    sp.edit().clear().apply()
}

/**
 * Get value from shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key for getting
 * @param defValue Value which will be returned if value by key is absent
 * @return int value if exists or defValue
 */
fun getValue(context: Context, key: String, defValue: Int): Int {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
    return sp.getInt(key, defValue)
}

/**
 * Get value from shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key for getting
 * @param defValue Value which will be returned if value by key is absent
 * @return boolean value if exists or defValue
 */
fun getValue(context: Context, key: String, defValue: Boolean): Boolean {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
    return sp.getBoolean(key, defValue)
}

/**
 * Get value from shared preferences.
 *
 * @param context Context for getting shared preferences
 * @param key Key for getting
 * @param defValue Value which will be returned if value by key is absent
 * @return String value if exists or defValue
 */
fun getValue(context: Context, key: String, defValue: String?): String {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
    return sp.getString(key, defValue) ?: defValue ?: error("Can't get value for key $key")
}

/**
 * Remove value from shared preferences by key.
 *
 * @param context Context for getting shared preferences
 * @param key Key for removing
 */
fun removeKeyValue(context: Context, key: String) {
    val sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
    sp.edit().remove(key).apply()
}

/**
 * Utils for shared preferences
 *
 * @version $Id: f6a4505e6593fc031f5b944abcf6ab3632b3014b $
 */
class SharedPrefsUtils

