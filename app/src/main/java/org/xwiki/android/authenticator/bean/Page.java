package org.xwiki.android.authenticator.bean;

/**
 * Created by fitz on 2016/4/25.
 */
public class Page {
    public String id;
    public String name;
    public String lastModified;

    @Override
    public String toString() {
        return "Page{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lastModified='" + lastModified + '\'' +
                '}';
    }
}
