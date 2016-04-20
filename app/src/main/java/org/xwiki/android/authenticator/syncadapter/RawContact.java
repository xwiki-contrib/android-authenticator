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
package org.xwiki.android.authenticator.syncadapter;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import java.lang.StringBuilder;

/**
 * Represents a low-level contacts RawContact - or at least
 * the fields of the RawContact that we care about.
 */
final public class RawContact {

    /** The tag used to log to adb console. **/
    private static final String TAG = "RawContact";

    private final String mUserName;

    private final String mFullName;

    private final String mFirstName;

    private final String mLastName;

    private final String mCellPhone;

    private final String mOfficePhone;

    private final String mHomePhone;

    private final String mEmail;

    private final String mStatus;

    private final String mAvatarUrl;

    private final boolean mDeleted;

    private final boolean mDirty;

    private final long mServerContactId;

    private final long mRawContactId;

    private final long mSyncState;

    public long getServerContactId() {
        return mServerContactId;
    }

    public long getRawContactId() {
        return mRawContactId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getFullName() {
        return mFullName;
    }

    public String getCellPhone() {
        return mCellPhone;
    }

    public String getOfficePhone() {
        return mOfficePhone;
    }

    public String getHomePhone() {
        return mHomePhone;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public long getSyncState() {
        return mSyncState;
    }



    public String getBestName() {
        if (!TextUtils.isEmpty(mFullName)) {
            return mFullName;
        } else if (TextUtils.isEmpty(mFirstName)) {
            return mLastName;
        } else {
            return mFirstName;
        }
    }

    public RawContact(String name, String fullName, String firstName, String lastName,
            String cellPhone, String officePhone, String homePhone, String email,
            String status, String avatarUrl, boolean deleted, long serverContactId,
            long rawContactId, long syncState, boolean dirty) {
        mUserName = name;
        mFullName = fullName;
        mFirstName = firstName;
        mLastName = lastName;
        mCellPhone = cellPhone;
        mOfficePhone = officePhone;
        mHomePhone = homePhone;
        mEmail = email;
        mStatus = status;
        mAvatarUrl = avatarUrl;
        mDeleted = deleted;
        mServerContactId = serverContactId;
        mRawContactId = rawContactId;
        mSyncState = syncState;
        mDirty = dirty;
    }

    /**
     * Creates and returns an instance of the RawContact from the provided JSON data.
     *
     * @param contact The JSONObject containing user data
     * @return user The new instance of Sample RawContact created from the JSON data.
     */
    public static RawContact valueOf(JSONObject contact) {

        try {
            final String userName = !contact.isNull("u") ? contact.getString("u") : null;
            final int serverContactId = !contact.isNull("i") ? contact.getInt("i") : -1;
            // If we didn't get either a username or serverId for the contact, then
            // we can't do anything with it locally...
            if ((userName == null) && (serverContactId <= 0)) {
                throw new JSONException("JSON contact missing required 'u' or 'i' fields");
            }

            final int rawContactId = !contact.isNull("c") ? contact.getInt("c") : -1;
            final String firstName = !contact.isNull("f")  ? contact.getString("f") : null;
            final String lastName = !contact.isNull("l") ? contact.getString("l") : null;
            final String cellPhone = !contact.isNull("m") ? contact.getString("m") : null;
            final String officePhone = !contact.isNull("o") ? contact.getString("o") : null;
            final String homePhone = !contact.isNull("h") ? contact.getString("h") : null;
            final String email = !contact.isNull("e") ? contact.getString("e") : null;
            final String status = !contact.isNull("s") ? contact.getString("s") : null;
            final String avatarUrl = !contact.isNull("a") ? contact.getString("a") : null;
            final boolean deleted = !contact.isNull("d") ? contact.getBoolean("d") : false;
            final long syncState = !contact.isNull("x") ? contact.getLong("x") : 0;
            return new RawContact(userName, null, firstName, lastName, cellPhone,
                    officePhone, homePhone, email, status, avatarUrl, deleted,
                    serverContactId, rawContactId, syncState, false);
        } catch (final Exception ex) {
            Log.i(TAG, "Error parsing JSON contact object" + ex.toString());
        }
        return null;
    }
}
