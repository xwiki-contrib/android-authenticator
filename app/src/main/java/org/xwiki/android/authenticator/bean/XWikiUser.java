package org.xwiki.android.authenticator.bean;

public class XWikiUser {
    public String id;  //pageId xwiki:XWiki.LudovicDubost (wiki:space.pageName)

    public String wiki;

    public String space;

    public String pageName;  //LudovicDubost

    public String firstName;

    public String lastName;

    public String email;

    public String phone;

    public String lastModifiedDate;

    public String avatar;

    public String company;

    public String blog;

    public String blogFeed;

    public long rawId;

    @Override
    public String toString() {
        return "XWikiUser{" +
                "id='" + id + '\'' +
                ", wiki='" + wiki + '\'' +
                ", space='" + space + '\'' +
                ", pageName='" + pageName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", avatar='" + avatar + '\'' +
                ", company='" + company + '\'' +
                ", blog='" + blog + '\'' +
                ", blogFeed='" + blogFeed + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getWiki() {
        return wiki;
    }

    public String getSpace() {
        return space;
    }

    public String getPageName() {
        return pageName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getCompany() {
        return company;
    }

    public String getBlog() {
        return blog;
    }

    public String getBlogFeed() {
        return blogFeed;
    }

    /**
     * id(curriki:XWiki.Luisafan)->[wiki,space,pageName]
     * @param id
     * @return 0:wiki 1:space 2:pageName
     */
    public static String[] splitId(String id){
        String[] result = new String[3];
        String[] strs = id.split(":");
        if(strs.length == 2){
            result[0] = strs[0];
            String[] strs2 = strs[1].split("\\.");
            if(strs2.length == 2){
                result[1] = strs2[0];
                result[2] = strs2[1];
                return result;
            }
        }
        return null;
    }
}
