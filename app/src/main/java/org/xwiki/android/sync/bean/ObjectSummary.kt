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
package org.xwiki.android.sync.bean

/**
 * ObjectSummary
 */
class ObjectSummary {
    var id: String? = null

    var guid: String? = null

    var pageId: String? = null

    var pageVersion: String? = null

    var wiki: String? = null

    var space: String? = null

    var pageName: String? = null

    var pageAuthor: String? = null

    var className: String? = null

    var number: String? = null

    var headline: String? = null

    override fun toString(): String {
        return "ObjectSummary{" +
                "id='" + id + '\''.toString() +
                ", guid='" + guid + '\''.toString() +
                ", pageId='" + pageId + '\''.toString() +
                ", pageVersion='" + pageVersion + '\''.toString() +
                ", wiki='" + wiki + '\''.toString() +
                ", space='" + space + '\''.toString() +
                ", pageName='" + pageName + '\''.toString() +
                ", pageAuthor='" + pageAuthor + '\''.toString() +
                ", className='" + className + '\''.toString() +
                ", number='" + number + '\''.toString() +
                ", headline='" + headline + '\''.toString() +
                '}'.toString()
    }
}
