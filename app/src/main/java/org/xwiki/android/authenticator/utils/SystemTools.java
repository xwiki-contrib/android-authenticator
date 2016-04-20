package org.xwiki.android.authenticator.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.telephony.TelephonyManager;

import java.io.File;
import java.security.MessageDigest;
import java.util.List;

/**
 * system tools
 *
 */
@SuppressLint("SimpleDateFormat")
public final class SystemTools {

    /**
     * Get IMEI
     */
    public static String getPhoneIMEI(Context cxt) {
        TelephonyManager tm = (TelephonyManager) cxt
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * get SDK version
     * 
     * @return if API 17 return 17
     */
    public static int getSDKVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * return System version
     * 
     * @return like 2.3.3
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * send sms
     */
    public static void sendSMS(Context cxt, String smsBody) {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", smsBody);
        cxt.startActivity(intent);
    }

    /**
     * check whether network is connected
     */
    public static boolean checkNet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null;
    }

    /**
     * check whether wifi is connected
     */
    public static boolean isWiFi(Context cxt) {
        ConnectivityManager cm = (ConnectivityManager) cxt
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // wifi status：ConnectivityManager.TYPE_WIFI
        // 3G status：ConnectivityManager.TYPE_MOBILE
        State state = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        return State.CONNECTED == state;
    }


    /**
     * foreground or background
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    // background
                    return true;
                } else {
                    // foreground
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * whether phone is sleeping
     */
    public static boolean isSleeping(Context context) {
        KeyguardManager kgMgr = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        boolean isSleeping = kgMgr.inKeyguardRestrictedInputMode();
        return isSleeping;
    }

    /**
     * apk install
     *
     * @param context
     * @param file
     */
    public static void installApk(Context context, File file) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType("application/vnd.android.package-archive");
        intent.setData(Uri.fromFile(file));
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * get app version name
     */
    public static String getAppVersionName(Context context) {
        String version = "0";
        try {
            version = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(SystemTools.class.getName()
                    + "the application not found");
        }
        return version;
    }

    /**
     * get app version code
     */
    public static int getAppVersionCode(Context context) {
        int version = 0;
        try {
            version = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(SystemTools.class.getName()
                    + "the application not found");
        }
        return version;
    }

    /**
     * return Home，run in background
     */
    public static void goHome(Context context) {
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(mHomeIntent);
    }

    /**
     * get application signature
     * 
     * @param context
     * @param pkgName
     */
    public static String getSign(Context context, String pkgName) {
        try {
            PackageInfo pis = context.getPackageManager().getPackageInfo(
                    pkgName, PackageManager.GET_SIGNATURES);
            return hexdigest(pis.signatures[0].toByteArray());
        } catch (NameNotFoundException e) {
            throw new RuntimeException(SystemTools.class.getName() + "the "
                    + pkgName + "'s application not found");
        }
    }

    /**
     * transfer signature to 32bit
     */
    private static String hexdigest(byte[] paramArrayOfByte) {
        final char[] hexDigits = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97,
                98, 99, 100, 101, 102 };
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramArrayOfByte);
            byte[] arrayOfByte = localMessageDigest.digest();
            char[] arrayOfChar = new char[32];
            for (int i = 0, j = 0;; i++, j++) {
                if (i >= 16) {
                    return new String(arrayOfChar);
                }
                int k = arrayOfByte[i];
                arrayOfChar[j] = hexDigits[(0xF & k >>> 4)];
                arrayOfChar[++j] = hexDigits[(k & 0xF)];
            }
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * available memory size
     * 
     * @param cxt
     *            context
     * @return memory size
     */
    public static int getDeviceUsableMemory(Context cxt) {
        ActivityManager am = (ActivityManager) cxt
                .getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return (int) (mi.availMem / (1024 * 1024));
    }

    /**
     * clean services and background
     * 
     * @param cxt
     *            context
     * @return clean app count
     */
    public static int gc(Context cxt) {
        long i = getDeviceUsableMemory(cxt);
        int count = 0; // clean app count
        ActivityManager am = (ActivityManager) cxt
                .getSystemService(Context.ACTIVITY_SERVICE);
        // get services which are running.
        List<RunningServiceInfo> serviceList = am.getRunningServices(100);
        if (serviceList != null)
            for (RunningServiceInfo service : serviceList) {
                if (service.pid == android.os.Process.myPid())
                    continue;
                try {
                    android.os.Process.killProcess(service.pid);
                    count++;
                } catch (Exception e) {
                    e.getStackTrace();
                    continue;
                }
            }

        // get processes which are running
        List<RunningAppProcessInfo> processList = am.getRunningAppProcesses();
        if (processList != null)
            for (RunningAppProcessInfo process : processList) {
                // >RunningAppProcessInfo.IMPORTANCE_SERVICE  -> NULL process
                // >RunningAppProcessInfo.IMPORTANCE_VISIBLE -> background process
                if (process.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    // pkgList get packages in this process
                    String[] pkgList = process.pkgList;
                    for (String pkgName : pkgList) {
                        Loger.debug("======killing：" + pkgName);
                        try {
                            am.killBackgroundProcesses(pkgName);
                            count++;
                        } catch (Exception e) {
                            e.getStackTrace();
                            continue;
                        }
                    }
                }
            }
        Loger.debug("Clean" + (getDeviceUsableMemory(cxt) - i) + "M memory!");
        return count;
    }
}