package org.xwiki.android.authenticator.bean;

public class XWikiUser {
    public String id;

    public String serverId;

    public String fullName;

    public String first_name;

    public String last_name;

    public String email;

    public String phone;

    public String date;

    public String avatarUrl;

    @Override
    public String toString() {
        return "XWikiUsers{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", date='" + date + '\'' +
                '}';
    }


    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDate() {
        return date;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getServerId() {
        return serverId;
    }
}
