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

import org.xwiki.android.sync.AppContext;

import java.util.ArrayList;
import java.util.List;

public class XWikiUserFull {
    public String id;
    public String pageName;
    public Integer number;

    //to be sure that here will be at least empty list
    private List<Property> properties = new ArrayList<>();

    public Boolean isActive() {
        String value = searchValue("active");
        return value != null && value.equals("1");
    }

    public String getCountry() {
        return searchValue("country");
    }

    public void setCountry(String country) {
        setValue("country", country);
    }

    public String getCity() {
        return searchValue("city");
    }

    public void setCity(String city) {
        setValue("city", city);
    }

    public String getAddress() {
        return searchValue("address");
    }

    public void setAddress(String address) {
        setValue("address", address);
    }

    public String getCompany() {
        return searchValue("company");
    }

    public void setCompany(String company) {
        setValue("company", company);
    }

    public String getComment() {
        return searchValue("comment");
    }

    public void setComment(String comment) {
        setValue("comment", comment);
    }

    public String getEmail() {
        return searchValue("email");
    }

    public void setEmail(String email) {
        setValue("email", email);
    }

    public String getFirstName() {
        return searchValue("first_name");
    }

    public void setFirstName(String firstName) {
        setValue("first_name", firstName);
    }

    public String getLastName() {
        return searchValue("last_name");
    }

    public void setLastName(String lastName) {
        setValue("last_name", lastName);
    }

    public String getFullName() {
        return searchValue("fullname");
    }

    public void setFullName(String fullName) {
        setValue("fullname", fullName);
    }

    public String getPhone() {
        return searchValue("phone");
    }

    public void setPhone(String phone) {
        setValue("phone", phone);
    }

    /**
     * @return Avatar URL without base url
     */
    public String getAvatar() {
        return "bin/download/XWiki/" + pageName
            + "/" + searchValue("avatar");
    }

    /**
     * Search value in {@link #properties}
     *
     * @param key Key to search property
     * @return Value or null
     */
    private String searchValue(String key) {
        for (Property property : properties) {
            if (property.name != null && property.name.equals(key)) {
                return property.value;
            }
        }
        return null;
    }

    /**
     * Representtion of {@link #setValue(String, String, String)} where type is "string"
     * @param key Key to search property or insert new
     * @param value Value of property
     */
    private void setValue(String key, String value) {
        setValue(key, value, "string");
    }

    /**
     * Set value for existing property or create new
     *
     * @param key Key to search property or insert new
     * @param value Value of property
     * @param type Type of property
     */
    private void setValue(String key, String value, String type) {
        for (Property property : properties) {
            if (property.name != null && property.name.equals(key)) {
                property.value = value;
                return;
            }
        }
        Property property = new Property();
        property.name = key;
        property.value = value;
        property.type = type;

        KeyValueObject nameKeyValue = new KeyValueObject();
        nameKeyValue.key = "name";
        nameKeyValue.value = property.name;
        property.attributes.add(nameKeyValue);

        KeyValueObject valueKeyValue = new KeyValueObject();
        valueKeyValue.key = "value";
        valueKeyValue.value = property.value;
        property.attributes.add(valueKeyValue);

        KeyValueObject typeKeyValue = new KeyValueObject();
        typeKeyValue.key = "type";
        typeKeyValue.value = property.type;
        property.attributes.add(typeKeyValue);

        properties.add(
            property
        );
    }
}
