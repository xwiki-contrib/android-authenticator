package org.xwiki.android.authenticator.bean;

/**
 * Created by fitz on 2016/4/20.
 */
public class XWikiGroup {
    public String id; // curriki:XWiki.XWikiAdminGroup

    public String wiki;

    public String space;

    public String pageName;

    public String lastModifiedDate;

    public String version;

    @Override
    public String toString() {
        return "XWikiGroup{" +
                "id='" + id + '\'' +
                ", wiki='" + wiki + '\'' +
                ", space='" + space + '\'' +
                ", pageName='" + pageName + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                '}';
    }
}
