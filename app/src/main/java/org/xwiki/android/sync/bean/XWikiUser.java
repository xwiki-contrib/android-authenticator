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

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.AbstractMap;
import java.util.Map;

public class XWikiUser {
    /**
     * pageId xwiki:XWiki.LudovicDubost (wiki:space.pageName)
     */
    public String id;

    public String wiki;

    public String space;
    /**
     * LudovicDubost
     */
    public String pageName;

    public String firstName;

    public String lastName;

    public String email;

    public String phone;

    @SerializedName("modified")
    public String lastModifiedDate;

    public String avatar;

    public String company;

    public String blog;

    public String blogFeed;

    public long rawId;

    public XWikiUser() {
    }

    public XWikiUser(String id, String pageName, String firstName, String lastName, String email, String phone, String avatar, long rawId, String company, String blog, String wiki, String space, String lastModifiedDate, String blogFeed) {
        this.id = id;
        this.pageName = pageName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.rawId = rawId;
        this.company = company;
        this.blog = blog;
        this.wiki = wiki;
        this.space = space;
        this.lastModifiedDate = lastModifiedDate;
        this.blogFeed = blogFeed;
    }

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
     *
     * @param id
     * @return 0:wiki 1:space 2:pageName
     */
    public static String[] splitId(String id) {
        String wiki = null;
        String space = null;
        String pageName = null;
        if (id.contains(":")) {
            String[] splittedWithWiki = id.split(":");
            wiki = splittedWithWiki[0];
            String[] spaceAndPageName = splittedWithWiki[1].split("\\.");
            space = spaceAndPageName[0];
            pageName = spaceAndPageName[1];
        } else {
            String[] spaceAndPageName = id.split("\\.");
            space = spaceAndPageName[0];
            pageName = spaceAndPageName[1];
        }
        return new String[]{wiki, space, pageName};
    }

    @Nullable
    public static Map.Entry<String, String> spaceAndPage(String id) {
        String[] splitted = splitId(id);
        if (splitted != null) {
            return new AbstractMap.SimpleEntry<>(splitted[1], splitted[2]);
        } else {
            return null;
        }
    }
}
