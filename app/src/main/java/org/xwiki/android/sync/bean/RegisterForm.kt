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
package org.xwiki.android.sync.bean

import com.google.gson.annotations.SerializedName

class RegisterForm {

    @SerializedName("form_token")
    var formToken: String

    var parent = "xwiki:Main.UserDirectory"

    @SerializedName("register_first_name")
    var firstName: String

    @SerializedName("register_last_name")
    var lastName: String

    @SerializedName("xwikiname")
    var xwiniName: String

    @SerializedName("register_password")
    var password: String

    @SerializedName("register2_password")
    var passwordRepeat: String

    @SerializedName("register_email")
    var email: String

    @SerializedName("captcha_answer")
    var captcha: String

    var template = "XWiki.XWikiUserTemplate"

    var xredirect = "/xwiki/bin/view/Main/UserDirectory"

    constructor(
            formToken: String,
            firstName: String,
            lastName: String,
            xwiniName: String,
            password: String,
            passwordRepeat: String,
            email: String,
            captcha: String
    ) {
        this.formToken = formToken
        this.firstName = firstName
        this.lastName = lastName
        this.xwiniName = xwiniName
        this.password = password
        this.passwordRepeat = passwordRepeat
        this.email = email
        this.captcha = captcha
    }

    constructor(
            formToken: String,
            firstName: String,
            lastName: String,
            xwiniName: String,
            password: String,
            email: String,
            captcha: String
    ) {
        this.formToken = formToken
        this.firstName = firstName
        this.lastName = lastName
        this.xwiniName = xwiniName
        this.password = password
        this.passwordRepeat = password
        this.email = email
        this.captcha = captcha
    }
}
