package org.xwiki.android.authenticator.rest;

import org.xwiki.android.authenticator.bean.XWikiUserFull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncData {
    //the users which have been modified from the last sync time. mainly used for updating and adding..
    private List<XWikiUserFull> updateUserList = new ArrayList<>();

    private Set<String> additionalIds = new HashSet<>();

    public List<XWikiUserFull> getUpdateUserList() {
        return updateUserList;
    }

    public Set<String> getAllIdSet() {
        Set<String> idsSet = new HashSet<>(additionalIds);
        for (XWikiUserFull user : getUpdateUserList()) {
            idsSet.add(user.id);
        }
        return idsSet;
    }

    public void addAdditionalId(String id) {
        additionalIds.add(id);
    }

    @Override
    public String toString() {
        return "SyncData{" +
            "updateUserList=" + updateUserList +
            '}';
    }
}
