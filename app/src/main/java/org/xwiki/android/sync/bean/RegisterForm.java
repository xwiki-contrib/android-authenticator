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

import com.google.gson.annotations.SerializedName;

public class RegisterForm {

    @SerializedName("form_token")
    public String formToken;

    public String parent = "xwiki:Main.UserDirectory";

    @SerializedName("register_first_name")
    public String firstName;

    @SerializedName("register_last_name")
    public String lastName;

    @SerializedName("xwikiname")
    public String xwiniName;

    @SerializedName("register_password")
    public String password;

    @SerializedName("register2_password")
    public String passwordRepeat;

    @SerializedName("register_email")
    public String email;

    @SerializedName("captcha_answer")
    public String captcha;

    public String template = "XWiki.XWikiUserTemplate";

    public String xredirect = "/xwiki/bin/view/Main/UserDirectory";

    public RegisterForm(
        String formToken,
        String firstName,
        String lastName,
        String xwiniName,
        String password,
        String passwordRepeat,
        String email,
        String captcha
    ) {
        this.formToken = formToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.xwiniName = xwiniName;
        this.password = password;
        this.passwordRepeat = passwordRepeat;
        this.email = email;
        this.captcha = captcha;
    }

    public RegisterForm(
        String formToken,
        String firstName,
        String lastName,
        String xwiniName,
        String password,
        String email,
        String captcha
    ) {
        this.formToken = formToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.xwiniName = xwiniName;
        this.password = password;
        this.passwordRepeat = password;
        this.email = email;
        this.captcha = captcha;
    }
}
