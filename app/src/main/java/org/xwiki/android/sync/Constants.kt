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

/**
 * @version $Id: b5f3c64375bf824faab782e804c73de41ce33a4b $
 */
open class Constants {

    companion object {

        /**
         * Account type id
         */
        val ACCOUNT_TYPE : String = "org.xwiki.android.sync"

        /**
         * Account name
         */
        val ACCOUNT_NAME : String = "XWiki"
        val USERDATA_SERVER : String = "XWIKI_SERVER"

        /**
         * Auth token types
         */
        val AUTHTOKEN_TYPE_READ_ONLY : String = "Read only"
        val AUTHTOKEN_TYPE_READ_ONLY_LABEL : String = "Read only access to an XWiki account"
        val AUTHTOKEN_TYPE_FULL_ACCESS : String = "Full access"
        val AUTHTOKEN_TYPE_FULL_ACCESS_LABEL : String = "Full access to an XWiki account"

        /**
         * limit the number of users synchronizing from server.
         */
        @JvmField
        var LIMIT_MAX_SYNC_USERS: Int = 10000

        /**
         * SyncType
         * 0: no sync, 1: all users, 2: sync groups
         */

        @JvmField
        var SYNC_TYPE_ALL_USERS: Int = 0
        @JvmField
        var SYNC_TYPE_SELECTED_GROUPS: Int = 1
        @JvmField
        var SYNC_TYPE_NO_NEED_SYNC: Int = 2

        /**
         * sync maker
         */
        var SYNC_MARKER_KEY: String = "org.xwiki.android.sync.marker"

        /**
         * sharePreference
         */
        var PACKAGE_LIST: String = "packageList"
        var SERVER_ADDRESS: String = "requestUrl"
        var SELECTED_GROUPS: String = "SelectGroups"
        var SYNC_TYPE: String = "SyncType"
        var COOKIE: String = "Cookie"


        /**
         * sync interval
         */
        @JvmField
        var SYNC_INTERVAL: Int = 60 * 60 //half an hour

    }

}
