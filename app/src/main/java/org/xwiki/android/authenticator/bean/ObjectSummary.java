package org.xwiki.android.authenticator.bean;

/**
 * Created by fitz on 2016/4/25.
 */
public class ObjectSummary {
    public String id;

    public String guid;

    public String pageId;

    public String pageVersion;

    public String wiki;

    public String space;

    public String pageName;

    public String pageAuthor;

    public String className;

    public String number;

    public String headline;

    @Override
    public String toString() {
        return "ObjectSummary{" +
                "id='" + id + '\'' +
                ", guid='" + guid + '\'' +
                ", pageId='" + pageId + '\'' +
                ", pageVersion='" + pageVersion + '\'' +
                ", wiki='" + wiki + '\'' +
                ", space='" + space + '\'' +
                ", pageName='" + pageName + '\'' +
                ", pageAuthor='" + pageAuthor + '\'' +
                ", className='" + className + '\'' +
                ", number='" + number + '\'' +
                ", headline='" + headline + '\'' +
                '}';
    }
}
