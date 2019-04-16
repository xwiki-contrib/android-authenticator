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
package org.xwiki.android.sync.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class for helping with permissions
 *
 * @version $Id$
 */
public class PermissionsUtils {

    /**
     * Context of permission operations
     */
    private Activity activity;

    /**
     * Array of permissions which this application required
     */
    private String[] mRequiredPermissions;

    /**
     * Permissions which was not given
     */
    private List<String> mPermissionsToRequest = new ArrayList<>();

    /**
     * Standard constructor. Fill {@link #mRequiredPermissions} from {@link PackageInfo}.
     *
     * @param activity Will be set to {@link #activity} and used to get permissions
     * @throws IllegalArgumentException Will be thrown when activity can't be used
     *
     * @since 0.4
     */
    public PermissionsUtils(Activity activity) throws IllegalArgumentException {
        this.activity = activity;
        try {
            PackageInfo info = activity
                    .getPackageManager()
                    .getPackageInfo(
                            activity.getPackageName(),
                            PackageManager.GET_PERMISSIONS
                    );
            mRequiredPermissions = info.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(PermissionsUtils.class.getSimpleName(), "Can't get package of input activity");
            throw new IllegalArgumentException("This activity can't be used as source of app context");
        }
    }

    /**
     * Checks if all the required permissions are granted.
     *
     * @return true if all the required permissions are granted, otherwise false
     */
    public boolean checkPermissions() {
        for (String permission : mRequiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionsToRequest.add(permission);
            }
        }

        return mPermissionsToRequest.isEmpty();
    }

    /**
     * Requests the missing permissions.
     * @param requestCode request code used by the activity
     *
     * @see Activity#onActivityResult(int, int, Intent)
     * @see ActivityCompat#requestPermissions(Activity, String[], int)
     */
    public void requestPermissions(int requestCode) {
        String[] request = mPermissionsToRequest.toArray(new String[mPermissionsToRequest.size()]);

        StringBuilder log = new StringBuilder();
        log.append("Requesting permissions:\n");

        for (String permission : request) {
            log.append(permission).append("\n");
        }

        Log.i(getClass().getSimpleName(), log.toString());

        ActivityCompat.requestPermissions(activity, request, requestCode);
    }
}
