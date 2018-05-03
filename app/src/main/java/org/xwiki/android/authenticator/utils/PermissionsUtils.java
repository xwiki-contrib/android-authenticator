package org.xwiki.android.authenticator.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * PermissionsCheck.
 */
public class PermissionsUtils {
    private Activity mContext;
    private String[] mRequiredPermissions;
    private List<String> mPermissionsToRequest = new ArrayList<>();

    public PermissionsUtils(Activity context, String... requiredPermissions) {
        mContext = context;
        mRequiredPermissions = requiredPermissions;
    }

    public PermissionsUtils(Activity activity) throws IllegalArgumentException {
        mContext = activity;
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
     * @return true if all the required permissions are granted, otherwise false
     */
    public boolean checkPermissions() {
        for (String permission : mRequiredPermissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionsToRequest.add(permission);
            }
        }

        if (mPermissionsToRequest.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Requests the missing permissions.
     * The activity from which this method is called has to implement
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * and then, inside it, it has to call the method
     * {@link PermissionsUtils#areAllRequiredPermissionsGranted(int[])} to check that all the
     * requested permissions are granted by the user
     * @param requestCode request code used by the activity
     */
    public void requestPermissions(int requestCode) {
        String[] request = mPermissionsToRequest.toArray(new String[mPermissionsToRequest.size()]);

        StringBuilder log = new StringBuilder();
        log.append("Requesting permissions:\n");

        for (String permission : request) {
            log.append(permission).append("\n");
        }

        Log.i(getClass().getSimpleName(), log.toString());

        ActivityCompat.requestPermissions(mContext, request, requestCode);
    }

    /**
     * Method to call inside
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}, to check if the
     * required permissions are granted.
     * @param grantResults results
     * @return true if all the required permissions are granted, otherwise false
     */
    public boolean areAllRequiredPermissionsGranted(int[] grantResults) {
        if (grantResults == null || grantResults.length == 0) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

}
