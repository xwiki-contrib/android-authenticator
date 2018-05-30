package org.xwiki.android.authenticator.bean;

import java.util.ArrayList;
import java.util.List;

public class XWikiUserFull {
    public String id;
    public String guid;
    public String pageId;
    public String pageVersion;
    public String wiki;
    public String space;
    public String pageName;
    public String pageAuthor;
    public String pageAuthorName;
    public String className;
    public Integer number;
    public String headline;

    //to be sure that here will be at least empty list
    public List<Property> properties = new ArrayList<>();

    public Boolean isActive() {
        String value = searchValue("active");
        if (value != null && value.equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    public String getCountry() {
        return searchValue("country");
    }

    public String getCity() {
        return searchValue("city");
    }

    public String getEmail() {
        return searchValue("email");
    }

    public String getFirstName() {
        return searchValue("first_name");
    }

    public String getLastName() {
        return searchValue("last_name");
    }

    public String getFullName() {
        return searchValue("fullname");
    }

    public String getPhone() {
        return searchValue("phone");
    }

    public String getAvatar() {
        return searchValue("avatar");
    }

    private String searchValue(String key) {
        for (Property property : properties) {
            if (property.name != null && property.name.equals(key)) {
                return property.value;
            }
        }
        return null;
    }
}
