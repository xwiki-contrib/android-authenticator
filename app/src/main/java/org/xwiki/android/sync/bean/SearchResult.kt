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
 *
 *  SearchResult class
 *
 *
 * <searchResult>
 * <link href="http://xwikichina.com/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/AileenNian" rel="http://www.xwiki.org/rel/page"></link>
 * <type>page</type>
 * <id>xwiki:XWiki.AileenNian</id>
 * <pageFullName>XWiki.AileenNian</pageFullName>
 * <wiki>xwiki</wiki>
 * <space>XWiki</space>
 * <pageName>AileenNian</pageName>
 * <modified>2016-01-29T13:09:32+08:00</modified>
 * <author>xwiki:XWiki.AileenNian</author>
 * <version>2.1</version>
 * <score>3.4895625</score>
</searchResult> *
 */
class SearchResult {
    var type: String? = null

    var id: String? = null

    var pageFullName: String? = null

    var wiki: String? = null

    var space: String? = null

    var pageName: String? = null

    var modified: String? = null

    var author: String? = null

    var version: String? = null

    var score: String? = null

    override fun toString(): String {
        return "SearchResult{" +
                "type='" + type + '\''.toString() +
                ", id='" + id + '\''.toString() +
                ", pageFullName='" + pageFullName + '\''.toString() +
                ", wiki='" + wiki + '\''.toString() +
                ", space='" + space + '\''.toString() +
                ", pageName='" + pageName + '\''.toString() +
                ", modified='" + modified + '\''.toString() +
                ", author='" + author + '\''.toString() +
                ", version='" + version + '\''.toString() +
                ", score='" + score + '\''.toString() +
                '}'.toString()
    }
}
