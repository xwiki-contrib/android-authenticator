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

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * System tools.
 *
 * @version $Id$
 */
public final class SystemTools {

    /**
     * Get from package version name
     *
     * @param context Context to get version name
     * @return version as how it was present in user settings
     */
    public static String getAppVersionName(Context context) {
        try {
            return context
                .getPackageManager()
                .getPackageInfo(
                    context.getPackageName(),
                    0
                )
                .versionName;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(SystemTools.class.getName() + "the application not found");
        }
    }
}