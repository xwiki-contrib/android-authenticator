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
 * Account type id
 */
const val ACCOUNT_TYPE = "org.xwiki.android.sync"

/**
 * Account name
 */
const val ACCOUNT_NAME = "XWiki"
const val USERDATA_SERVER = "XWIKI_SERVER"

const val XWIKI_DEFAULT_SERVER_ADDRESS = "https://www.xwiki.org/xwiki"

const val USER_TABLE = "user_table"

const val ALL_USERS_LIST_TABLE = "all_users_list_table"

const val GROUPS_LIST_TABLE = "groups_list_table"

/**
 * Auth token types
 */
const val AUTHTOKEN_TYPE_READ_ONLY = "Read only"
const val AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an XWiki account"
const val AUTHTOKEN_TYPE_FULL_ACCESS = "Full access"
const val AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an XWiki account"

/**
 * limit the number of users synchronizing from server.
 */
const val LIMIT_MAX_SYNC_USERS = 10000

/**
 * SyncType
 * 0: no sync, 1: all users, 2: sync groups
 */

const val SYNC_TYPE_ALL_USERS = 0
const val SYNC_TYPE_SELECTED_GROUPS = 1
const val SYNC_TYPE_NO_NEED_SYNC = 2

/**
 * sync maker
 */
const val SYNC_MARKER_KEY = "org.xwiki.android.sync.marker"

/**
 * sharePreference
 */
const val PACKAGE_LIST = "packageList"
const val SERVER_ADDRESS = "requestUrl"
const val SELECTED_GROUPS = "SelectGroups"
const val SYNC_TYPE = "SyncType"
const val COOKIE = "Cookie"


/**
 * sync interval
 */
const val SYNC_INTERVAL = 1800 //half an hour
/**
 * @version $Id: b5f3c64375bf824faab782e804c73de41ce33a4b $
 */