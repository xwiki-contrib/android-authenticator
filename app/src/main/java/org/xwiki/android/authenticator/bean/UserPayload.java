package org.xwiki.android.authenticator.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;


public class UserPayload implements Parcelable {

    @SerializedName("className")
    private String className;

    @SerializedName("property#first_name")
    private String firstName;

    @SerializedName("property#last_name")
    private String lastName;

    @SerializedName("property#email")
    private String email;

    @SerializedName("property#phone")
    private String phone;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.className);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.email);
        dest.writeString(this.phone);
    }

    public UserPayload() {
    }

    protected UserPayload(Parcel in) {
        this.className = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
    }

    public static final Parcelable.Creator<UserPayload> CREATOR =
            new Parcelable.Creator<UserPayload>() {
                @Override
                public UserPayload createFromParcel(Parcel source) {
                    return new UserPayload(source);
                }

                @Override
                public UserPayload[] newArray(int size) {
                    return new UserPayload[size];
                }
            };
}
