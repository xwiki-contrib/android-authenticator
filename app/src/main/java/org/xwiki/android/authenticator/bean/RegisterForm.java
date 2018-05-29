package org.xwiki.android.authenticator.bean;

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
