/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync.bean;

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
