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

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


class UserPayload : Parcelable {

    @SerializedName("className")
    var className: String? = null

    @SerializedName("property#first_name")
    var firstName: String? = null

    @SerializedName("property#last_name")
    var lastName: String? = null

    @SerializedName("property#email")
    var email: String? = null

    @SerializedName("property#phone")
    var phone: String? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.className)
        dest.writeString(this.firstName)
        dest.writeString(this.lastName)
        dest.writeString(this.email)
        dest.writeString(this.phone)
    }

    protected constructor(`in`: Parcel) {
        this.className = `in`.readString()
        this.firstName = `in`.readString()
        this.lastName = `in`.readString()
        this.email = `in`.readString()
        this.phone = `in`.readString()
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<UserPayload> = object : Parcelable.Creator<UserPayload> {
            override fun createFromParcel(source: Parcel): UserPayload {
                return UserPayload(source)
            }

            override fun newArray(size: Int): Array<UserPayload?> {
                return arrayOfNulls(size)
            }
        }
    }
}
