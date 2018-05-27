package org.xwiki.android.authenticator.rest.new_rest;

import org.xwiki.android.authenticator.bean.XWikiUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SyncData {
    //the users which have been modified from the last sync time. mainly used for updating and adding..
    List<XWikiUser> updateUserList;
    //all the users in the server or all the users of the selected groups used for deleting.
    HashSet<String> allIdSet;

    public SyncData() {
        updateUserList = new ArrayList<>();
        allIdSet = new HashSet<>();
    }

    public List<XWikiUser> getUpdateUserList() {
        return updateUserList;
    }

    public HashSet<String> getAllIdSet() {
        return allIdSet;
    }

    @Override
    public String toString() {
        return "SyncData{" +
                "updateUserList=" + updateUserList +
                ", allIdSet=" + allIdSet +
                '}';
    }
}
