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

/**
 * @version $Id: 75a8139c61762e9c24bca94897df7331856e374f $
 */
object Constants {
    /**
     * Server
     */
    val SERVER_ADDRESS = "requestUrl"

    /**
     * Account type id
     */
    val ACCOUNT_TYPE = "org.xwiki.android.sync"

    /**
     * Auth token types
     */
    val AUTHTOKEN_TYPE_READ_ONLY = "Read only"
    val AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an XWiki account"

    val AUTHTOKEN_TYPE_FULL_ACCESS = "Full access" + "org.xwiki.android.authdemo"
    val AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an XWiki account"


}
