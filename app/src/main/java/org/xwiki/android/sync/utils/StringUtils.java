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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Utils
 *
 * @version $Id$
 */
public class StringUtils {

    /**
     * Email pattern.
     */
    private final static Pattern emailer = Pattern.compile(
        "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*"
    );

    /**
     * Date format for parse/print dates from/to strings
     *
     * @since 0.4.2
     */
    private final static SimpleDateFormat sdf = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssZ"
    );

    static {
        sdf.setTimeZone(
            TimeZone.getTimeZone("GMT")
        );
    }

    /**
     * @param input Source char sequence
     * @return true if input is null or contains only space symbols (space, newlines, tabs) or empty
     */
    public static boolean isEmpty(CharSequence input) {
        if (input == null || input.length() == 0)
            return true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * Check that input is email.
     *
     * @param input Char sequence which can be email
     * @return true if input is not empty and match to {@link #emailer} pattern
     */
    public static boolean isEmail(CharSequence input) {
        return !isEmpty(input) && emailer.matcher(input).matches();
    }

    /**
     * Convert date from Iso8601 format to {@link Date}. Example of Iso8601:
     * 2011-09-24T19:45:31+02:00
     *
     * @param iso8601 Input with date in Iso8601 format
     * @return Date from iso8601 or null if iso8601 is incorrect
     */
    public static Date iso8601ToDate(String iso8601) {
        if(isEmpty(iso8601)) return null;
        try {
            return sdf.parse(iso8601);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert date from {@link Date} to Iso8601. Example of Iso8601:
     * 2011-09-24T19:45:31+02:00
     *
     * @param date Date to format
     * @return String of date in iso8601 format
     */
    public static String dateToIso8601String(Date date) {
        return sdf.format(date);
    }
}
