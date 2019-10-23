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
package org.xwiki.android.sync.utils

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

/**
 * Util class for helping with permissions
 *
 * @version $Id: 52495bcbfec076477f51bf0a65539a83bd0c6d6b $
 */
class PermissionsUtils
/**
 * Standard constructor. Fill [.mRequiredPermissions] from [PackageInfo].
 *
 * @param activity Will be set to [.activity] and used to get permissions
 * @throws IllegalArgumentException Will be thrown when activity can't be used
 *
 * @since 0.4
 */
@Throws(IllegalArgumentException::class)
constructor (private val activity: Activity) {

    /**
     * Array of permissions which this application required
     */
    private var mRequiredPermissions: Array<String>

    /**
     * Permissions which was not given
     */
    private val mPermissionsToRequest = ArrayList<String>()

    init {
        try {
            val info = activity
                .packageManager
                .getPackageInfo(
                    activity.packageName,
                    PackageManager.GET_PERMISSIONS
                )
            mRequiredPermissions = info.requestedPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(PermissionsUtils::class.java.simpleName, "Can't get package of input activity")
            throw IllegalArgumentException("This activity can't be used as source of app context")
        }

    }

    /**
     * Checks if all the required permissions are granted.
     *
     * @return true if all the required permissions are granted, otherwise false
     */
    fun checkPermissions(): Boolean {
        for (permission in mRequiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionsToRequest.add(permission)
            }
        }

        return mPermissionsToRequest.isEmpty()
    }

    /**
     * Requests the missing permissions.
     * @param requestCode request code used by the activity
     *
     * @see Activity.onActivityResult
     * @see ActivityCompat.requestPermissions
     */
    fun requestPermissions(requestCode: Int) {
        val request = mPermissionsToRequest.toTypedArray()

        val log = StringBuilder()
        log.append("Requesting permissions:\n")

        for (permission in request) {
            log.append(permission).append("\n")
        }

        Log.i(javaClass.simpleName, log.toString())

        ActivityCompat.requestPermissions(activity, request, requestCode)
    }
}
