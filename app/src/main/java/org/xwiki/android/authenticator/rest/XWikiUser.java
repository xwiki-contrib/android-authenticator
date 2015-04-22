package org.xwiki.android.authenticator.rest;

public class XWikiUser {
    private String id;

    private String fullName;

    private String mail;

    private String phone;

    private long date;

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }

    public long getDate() {
        return date;
    }
}
