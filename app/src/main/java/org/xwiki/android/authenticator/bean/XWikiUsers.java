package org.xwiki.android.authenticator.bean;

public class XWikiUsers {
    public String id;

    public String fullName;

    public String first_name;

    public String last_name;

    public String mail;

    public String phone;

    public String date;

    @Override
    public String toString() {
        return "XWikiUsers{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", mail='" + mail + '\'' +
                ", phone='" + phone + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

}
