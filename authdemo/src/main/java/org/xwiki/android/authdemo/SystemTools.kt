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
package org.xwiki.android.authdemo

import android.Manifest
import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityManager.RunningServiceInfo
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat

import java.io.File
import java.security.MessageDigest

/**
 * system tools
 */
object SystemTools {
    private val TAG = "SystemTools"

    /**
     * return System version
     *
     * @return like 2.3.3
     */
    val systemVersion: String
        get() = android.os.Build.VERSION.RELEASE

    /**
     * Get IMEI
     *
     * @return Device ID if application have access permissions, null otherwise
     */
    fun getPhoneIMEI(cxt: Context): String? {
        val tm = cxt
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(
                cxt,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            null
        } else tm.deviceId
    }


    /**
     * check whether network is connected
     */
    fun checkNet(context: Context): Boolean {
        val cm = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        return info != null && info.isConnected
    }

    fun checkWifi(context: Context): Boolean {
        val mWifiManager = context
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = mWifiManager.connectionInfo
        val ipAddress = wifiInfo?.ipAddress ?: 0
        return mWifiManager.isWifiEnabled && ipAddress != 0
    }


    /**
     * foreground or background
     */
    fun isBackground(context: Context): Boolean {
        val activityManager = context
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager
            .runningAppProcesses
        for (appProcess in appProcesses) {
            if (appProcess.processName == context.packageName) {
                return appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND
            }
        }
        return false
    }

    /**
     * whether phone is sleeping
     */
    fun isSleeping(context: Context): Boolean {
        val kgMgr = context
            .getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return kgMgr.inKeyguardRestrictedInputMode()
    }

    /**
     * apk install
     *
     * @param context
     * @param file
     */
    fun installApk(context: Context, file: File) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.addCategory("android.intent.category.DEFAULT")
        intent.type = "application/vnd.android.package-archive"
        intent.data = Uri.fromFile(file)
        intent.setDataAndType(
            Uri.fromFile(file),
            "application/vnd.android.package-archive"
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * get app version name
     */
    fun getAppVersionName(context: Context): String {
        var version = "0"
        try {
            version = context.packageManager.getPackageInfo(
                context.packageName, 0
            ).versionName
        } catch (e: NameNotFoundException) {
            throw RuntimeException(SystemTools::class.java.name + "the application not found")
        }

        return version
    }

    /**
     * get app version code
     */
    fun getAppVersionCode(context: Context): Int {
        var version = 0
        try {
            version = context.packageManager.getPackageInfo(
                context.packageName, 0
            ).versionCode
        } catch (e: NameNotFoundException) {
            throw RuntimeException(SystemTools::class.java.name + "the application not found")
        }

        return version
    }

    /**
     * return Home，run in background
     */
    fun goHome(context: Context) {
        val mHomeIntent = Intent(Intent.ACTION_MAIN)
        mHomeIntent.addCategory(Intent.CATEGORY_HOME)
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        context.startActivity(mHomeIntent)
    }

    /**
     * get application signature
     *
     * @param context
     * @param pkgName
     */
    fun getSign(context: Context, pkgName: String): String? {
        try {
            val pis = context.packageManager.getPackageInfo(
                pkgName, PackageManager.GET_SIGNATURES
            )
            return hexdigest(pis.signatures[0].toByteArray())
        } catch (e: NameNotFoundException) {
            throw RuntimeException(
                SystemTools::class.java.name + "the "
                        + pkgName + "'s application not found"
            )
        }

    }

    /**
     * transfer signature to 32bit
     */
    private fun hexdigest(paramArrayOfByte: ByteArray): String {
        val hexDigits = charArrayOf(
            48.toChar(),
            49.toChar(),
            50.toChar(),
            51.toChar(),
            52.toChar(),
            53.toChar(),
            54.toChar(),
            55.toChar(),
            56.toChar(),
            57.toChar(),
            97.toChar(),
            98.toChar(),
            99.toChar(),
            100.toChar(),
            101.toChar(),
            102.toChar()
        )
        try {
            val localMessageDigest = MessageDigest.getInstance("MD5")
            localMessageDigest.update(paramArrayOfByte)
            val arrayOfByte = localMessageDigest.digest()
            val arrayOfChar = CharArray(32)
            var i = 0
            var j = 0
            while (true) {
                if (i >= 16) {
                    return String(arrayOfChar)
                }
                val k = arrayOfByte[i].toInt()
                arrayOfChar[j] = hexDigits[0xF and k.ushr(4)]
                arrayOfChar[++j] = hexDigits[k and 0xF]
                i++
                j++
            }
        } catch (e: Exception) {
        }

        return ""
    }

    /**
     * available memory size
     *
     * @param cxt context
     * @return memory size
     */
    fun getDeviceUsableMemory(cxt: Context): Int {
        val am = cxt
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = MemoryInfo()
        am.getMemoryInfo(mi)
        return (mi.availMem / (1024 * 1024)).toInt()
    }

    /**
     * clean services and background
     *
     * @param cxt context
     * @return clean app count
     */
    fun gc(cxt: Context): Int {
        val i = getDeviceUsableMemory(cxt).toLong()
        var count = 0 // clean app count
        val am = cxt
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // get services which are running.
        val serviceList = am.getRunningServices(100)
        if (serviceList != null)
            for (service in serviceList) {
                if (service.pid == android.os.Process.myPid())
                    continue
                try {
                    android.os.Process.killProcess(service.pid)
                    count++
                } catch (e: Exception) {
                    e.stackTrace
                    continue
                }

            }

        // get processes which are running
        val processList = am.runningAppProcesses
        if (processList != null)
            for (process in processList) {
                // >RunningAppProcessInfo.IMPORTANCE_SERVICE  -> NULL process
                // >RunningAppProcessInfo.IMPORTANCE_VISIBLE -> background process
                if (process.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    // pkgList get packages in this process
                    val pkgList = process.pkgList
                    for (pkgName in pkgList) {
                        Log.d(TAG, "======killing：$pkgName")
                        try {
                            am.killBackgroundProcesses(pkgName)
                            count++
                        } catch (e: Exception) {
                            e.stackTrace
                            continue
                        }

                    }
                }
            }
        Log.d(TAG, "Clean" + (getDeviceUsableMemory(cxt) - i) + "M memory!")
        return count
    }
}