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

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.net.ConnectivityManager

/**
 * Get from package version name
 *
 * @param context Context to get version name
 * @return version as how it was present in user settings
 */
fun getAppVersionName(context: Context): String {
    try {
        return context
            .packageManager
            .getPackageInfo(
                context.packageName,
                0
            )
            .versionName
    } catch (e: NameNotFoundException) {
        throw RuntimeException(SystemTools::class.java.name + "the application not found")
    }
}

fun checkNet(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = cm.activeNetworkInfo
    return info != null && info.isConnected
}

/**
 * System tools.
 *
 * @version $Id: eb9d8af9731a7f869eb33a9e6696dc354104c897 $
 */
class SystemTools