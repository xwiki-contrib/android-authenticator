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
package org.xwiki.android.authenticator;

/**
 * @version $Id: $
 */
public class Constants {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = "org.xwiki.android.authenticator";

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "XWiki";
    public static final String USERDATA_SERVER = "XWIKI_SERVER";

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an XWiki account";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an XWiki account";

    //limit the number of users synchronizing from server.
    public static final int LIMIT_MAX_SYNC_USERS = 10000;

    //SyncType
    //0: no sync, 1: all users, 2: sync groups
    public static final int SYNC_TYPE_NO_NEED_SYNC = 0;
    public static final int SYNC_TYPE_ALL_USERS = 1;
    public static final int SYNC_TYPE_SELECTED_GROUPS = 2;

    //sync maker
    public static final String SYNC_MARKER_KEY = "org.xwiki.android.sync.marker";


    //sharePreference
    public static final String APP_UID = "appuid";
    public static final String PACKAGE_LIST = "packageList";
    public static final String SERVER_ADDRESS = "requestUrl";
    public static final String SERVER_REST_URL = "ServerUrl";
    public static final String SELECTED_GROUPS = "SelectGroups";
    public static final String SYNC_TYPE = "SyncType";
    public static final String COOKIE = "Cookie";


}
