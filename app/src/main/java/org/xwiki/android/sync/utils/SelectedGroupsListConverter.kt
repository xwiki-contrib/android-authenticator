package org.xwiki.android.sync.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.XWikiGroup
import java.util.*


class SelectedGroupsListConverter{
    private val gson = Gson()
    @TypeConverter
    fun stringToList(data: String?): List<String> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<String>>() {

        }.type

        return gson.fromJson<List<String>>(data, listType)
    }

    @TypeConverter
    fun listToString(someObjects: List<String>): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToXWikiGroupList(data: String?): MutableList<XWikiGroup> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<MutableList<XWikiGroup>>() {

        }.type

        return gson.fromJson<MutableList<XWikiGroup>>(data, listType)
    }

    @TypeConverter
    fun XWikiGroupListToString(someObjects: MutableList<XWikiGroup>): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToXWikiAllUserList(data: String?): MutableList<ObjectSummary> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<MutableList<ObjectSummary>>() {

        }.type

        return gson.fromJson<MutableList<ObjectSummary>>(data, listType)
    }

    @TypeConverter
    fun XWikiAllUserListToString(someObjects: MutableList<ObjectSummary>): String {
        return gson.toJson(someObjects)
    }
}