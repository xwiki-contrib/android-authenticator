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
package org.xwiki.android.authenticator.rest;

import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.bean.ObjectSummary;
import org.xwiki.android.authenticator.bean.Page;
import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * XmlUtils
 */
public class XmlUtils {
    private static final String TAG = "XmlUtils";

    /**
     * Get SearchResults from XML
     *
     * @param inStream from XML (byte->inStream)
     * @return List<SearchResult>
     */
    public static List<SearchResult> getSearchResults(InputStream inStream) throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inStream, "UTF-8");
        int eventType = parser.getEventType();
        SearchResult currentSearchResult = null;
        List<SearchResult> SearchResults = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    SearchResults = new ArrayList<SearchResult>();
                    break;
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if (name.equalsIgnoreCase("SearchResult")) {
                        currentSearchResult = new SearchResult();
                    } else if (currentSearchResult != null) {
                        if (name.equalsIgnoreCase("type")) {
                            currentSearchResult.type = parser.nextText();
                        } else if (name.equalsIgnoreCase("id")) {
                            currentSearchResult.id = parser.nextText();
                        } else if (name.equalsIgnoreCase("pageFullName")) {
                            currentSearchResult.pageFullName = parser.nextText();
                        } else if (name.equalsIgnoreCase("wiki")) {
                            currentSearchResult.wiki = parser.nextText();
                        } else if (name.equalsIgnoreCase("space")) {
                            currentSearchResult.space = parser.nextText();
                        } else if (name.equalsIgnoreCase("pageName")) {
                            currentSearchResult.pageName = parser.nextText();
                        } else if (name.equalsIgnoreCase("modified")) {
                            currentSearchResult.modified = parser.nextText();
                        } else if (name.equalsIgnoreCase("author")) {
                            currentSearchResult.author = parser.nextText();
                        } else if (name.equalsIgnoreCase("version")) {
                            currentSearchResult.version = parser.nextText();
                        } else if (name.equalsIgnoreCase("score")) {
                            currentSearchResult.score = parser.nextText();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equalsIgnoreCase("SearchResult") && currentSearchResult != null) {
                        SearchResults.add(currentSearchResult);
                        currentSearchResult = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        inStream.close();
        return SearchResults;

    }

    /**
     * get XWikiUser from server's response xml.
     *
     * @param inStream xml byte->InputStream
     * @return XWikiUser
     */
    public static XWikiUser getXWikiUser(InputStream inStream) throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inStream, "UTF-8");
        int eventType = parser.getEventType();
        XWikiUser user = new XWikiUser();
        String name = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType != XmlPullParser.START_TAG) {
                eventType = parser.next();
                continue;
            }
            String tag = parser.getName();
            if (tag.equalsIgnoreCase("pageId")) {
                user.id = parser.nextText();
            } else if (tag.equalsIgnoreCase("wiki")) {
                user.wiki = parser.nextText();
            } else if (tag.equalsIgnoreCase("space")) {
                user.space = parser.nextText();
            } else if (tag.equalsIgnoreCase("pageName")) {
                user.pageName = parser.nextText();
            } else if (tag.equalsIgnoreCase("property")) {
                name = parser.getAttributeValue(null, "name");
            } else if (tag.equalsIgnoreCase("value")) {
                if (name.equalsIgnoreCase("phone")) {
                    user.phone = parser.nextText();
                } else if (name.equalsIgnoreCase("email")) {
                    user.email = parser.nextText();
                } else if (name.equalsIgnoreCase("last_name")) {
                    user.lastName = parser.nextText();
                } else if (name.equalsIgnoreCase("first_name")) {
                    user.firstName = parser.nextText();
                } else if (name.equalsIgnoreCase("company")) {
                    user.company = parser.nextText();
                } else if (name.equalsIgnoreCase("blog")) {
                    user.blog = parser.nextText();
                } else if (name.equalsIgnoreCase("blogfeed")) {
                    user.blogFeed = parser.nextText();
                } else if (name.equalsIgnoreCase("avatar")) {
                    user.avatar = parser.nextText();
                }
            }
            eventType = parser.next();
        }
        return user;

    }


    /**
     * get List<ObjectSummary> from xml
     *
     * @param inStream
     * @return List<ObjectSummary>
     * http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects/XWiki.XWikiGroups
     */
    public static List<ObjectSummary> getObjectSummarys(InputStream inStream) throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inStream, "UTF-8");
        int eventType = parser.getEventType();
        ObjectSummary objectSummary = null;
        List<ObjectSummary> objectResults = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    objectResults = new ArrayList<ObjectSummary>();
                    break;
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if (name.equalsIgnoreCase("objectSummary")) {
                        objectSummary = new ObjectSummary();
                    } else if (objectSummary != null) {
                        if (name.equalsIgnoreCase("id")) {
                            objectSummary.id = parser.nextText();
                        } else if (name.equalsIgnoreCase("guid")) {
                            objectSummary.guid = parser.nextText();
                        } else if (name.equalsIgnoreCase("pageId")) {
                            objectSummary.pageId = parser.nextText();
                        } else if (name.equalsIgnoreCase("pageVersion")) {
                            objectSummary.pageVersion = parser.nextText();
                        } else if (name.equalsIgnoreCase("wiki")) {
                            objectSummary.wiki = parser.nextText();
                        } else if (name.equalsIgnoreCase("space")) {
                            objectSummary.space = parser.nextText();
                        } else if (name.equalsIgnoreCase("pageName")) {
                            objectSummary.pageName = parser.nextText();
                        } else if (name.equalsIgnoreCase("pageAuthor")) {
                            objectSummary.pageAuthor = parser.nextText();
                        } else if (name.equalsIgnoreCase("className")) {
                            objectSummary.className = parser.nextText();
                        } else if (name.equalsIgnoreCase("number")) {
                            objectSummary.number = parser.nextText();
                        } else if (name.equalsIgnoreCase("headline")) {
                            objectSummary.headline = parser.nextText();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equalsIgnoreCase("objectSummary") && objectSummary != null) {
                        if (!TextUtils.isEmpty(objectSummary.headline)) {
                            objectResults.add(objectSummary);
                        }
                        objectSummary = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        inStream.close();
        return objectResults;
    }

    /**
     * get Page from xml
     *
     * @param inStream
     * @return Page
     */
    public static Page getPage(InputStream inStream) throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inStream, "UTF-8");
        int eventType = parser.getEventType();
        Page page = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if (name.equalsIgnoreCase("page")) {
                        page = new Page();
                    } else if (page != null) {
                        if (name.equalsIgnoreCase("id")) {
                            page.id = parser.nextText();
                        } else if (name.equalsIgnoreCase("name")) {
                            page.name = parser.nextText();
                        } else if (name.equalsIgnoreCase("modified")) {
                            page.lastModified = parser.nextText();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }
        inStream.close();
        return page;
    }

}
