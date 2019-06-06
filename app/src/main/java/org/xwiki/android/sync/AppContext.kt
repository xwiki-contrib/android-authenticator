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
package org.xwiki.android.sync

import android.app.Application
import android.util.Log
import org.xwiki.android.sync.rest.BaseApiManager
import org.xwiki.android.sync.utils.SharedPrefsUtils

import java.util.AbstractMap
import java.util.ArrayList

/**
 * Application class for authenticator
 *
 * @version $Id: c3a5996b1bce14d5c105a55f085115347c39c035 $
 */
open class AppContext : Application() {

    /**
     * Set [.instance] to this object.
     */
    override fun onCreate() {
        super.onCreate()
        appContextInstance = this
        Log.d(TAG, "on create")
    }

    companion object {

        /**
         * Entry pair Server address - Base Api Manager
         */
        private var baseApiManager: AbstractMap.SimpleEntry<String, BaseApiManager>? = null

        /**
         * Logging tag
         */
        private val TAG = "AppContext"

        /**
         * Instance of context to use it in static methods
         */
        private lateinit var appContextInstance : AppContext

        /**
         * @return known AppContext instance
         */
        fun getInstance(): AppContext? {
            return appContextInstance
        }

        /**
         * @return actual base url
         */
        fun currentBaseUrl(): String {
            return SharedPrefsUtils.getValue(appContextInstance, Constants.SERVER_ADDRESS, "")
        }

        /**
         * Add app as authorized
         *
         * @param packageName Application package name to add as authorized
         */
        fun addAuthorizedApp(packageName: String) {
            Log.d(TAG, "packageName=$packageName")
            var packageList: MutableList<String>? = SharedPrefsUtils.getArrayList(appContextInstance.applicationContext, Constants.PACKAGE_LIST)
            if (packageList == null) {
                packageList = ArrayList()
            }
            packageList.add(packageName)
            SharedPrefsUtils.putArrayList(appContextInstance.applicationContext, Constants.PACKAGE_LIST, packageList)
        }

        /**
         * Check that application with packageName is authorised.
         *
         * @param packageName Application package name
         * @return true if application was authorized
         */
        fun isAuthorizedApp(packageName: String): Boolean {
            val packageList = SharedPrefsUtils.getArrayList(
                appContextInstance.applicationContext,
                Constants.PACKAGE_LIST
            )
            return packageList != null && packageList.contains(packageName)
        }

        /**
         * @return Current [.baseApiManager] value or create new and return
         *
         * @since 0.4
         */
        fun getApiManager() : BaseApiManager {
                val url = currentBaseUrl()
                if (baseApiManager == null || baseApiManager!!.key != url) {
                    baseApiManager = AbstractMap.SimpleEntry(
                        url,
                        BaseApiManager(url)
                    )
                }
                return baseApiManager!!.value
        }
    }
}
