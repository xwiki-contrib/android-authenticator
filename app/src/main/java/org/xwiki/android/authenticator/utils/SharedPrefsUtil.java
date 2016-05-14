package org.xwiki.android.authenticator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SharedPreferences Util
 * @author fitz
 */
public class SharedPrefsUtil {
	public final static String SETTING = "Setting";
	public static void putValue(Context context,String key, int value) {
		 Editor sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
		 sp.putInt(key, value);
		 sp.commit();
	}
	public static void putValue(Context context,String key, boolean value) {
		 Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
		 sp.putBoolean(key, value);
		 sp.commit();
	}
	public static void putValue(Context context,String key, String value) {
		 Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
		 sp.putString(key, value);
		 sp.commit();
	}
	public static int getValue(Context context,String key, int defValue) {
		SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		int value = sp.getInt(key, defValue);
		return value;
	}
	public static boolean getValue(Context context,String key, boolean defValue) {
		SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		boolean value = sp.getBoolean(key, defValue);
		return value;
	}
	public static String getValue(Context context,String key, String defValue) {
		SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		String value = sp.getString(key, defValue);
		return value;
	}
    public static void putArrayList(Context context, String key, List<String> list) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        Editor edit=sp.edit();
        Set<String> set = new HashSet<String>();
        set.addAll(list);
        edit.putStringSet(key, set);
        edit.commit();
    }
    public static List<String> getArrayList(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        Set<String> set = sp.getStringSet(key, null);
        if(set == null) return null;
        ArrayList<String> list = new ArrayList<>(set);
        return list;
    }

	public static void clearAll(Context context){
		SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		sp.edit().clear().commit();
	}

	public static void removeKeyValue(Context context, String key){
		SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		sp.edit().remove(key).commit();
	}

}

