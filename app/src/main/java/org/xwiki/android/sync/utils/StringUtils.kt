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
package org.xwiki.android.sync.utils

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * String Utils
 *
 * @version $Id: f12d27b7d2cae40e24a852cf01fd1f1c7c1c1d7e $
 */
object StringUtils {

    /**
     * Email pattern.
     */
    private val emailer = Pattern.compile(
            "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*"
    )

    /**
     * Phone pattern.
     *
     * @since 0.5
     */
    private val phonePattern = Pattern.compile(
            "\\+?(([\\d\\-]+)|(\\([\\d\\-]+\\)))+"
    )

    /**
     * Date format for parse/print dates from/to strings
     *
     * @since 0.4.2
     */
    private val sdf = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ"
    )

    init {
        sdf.timeZone = TimeZone.getTimeZone("GMT")
    }

    /**
     * @param input Source char sequence
     * @return true if input is null or contains only space symbols (space, newlines, tabs) or empty
     */
    fun isEmpty(input: CharSequence?): Boolean {
        if (input == null || input.length == 0)
            return true
        for (i in 0 until input.length) {
            val c = input[i]
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false
            }
        }
        return true
    }

    /**
     * Make routine with convert empty string to null or return string
     *
     * @param input What do we work with
     * @return input as string if [.isEmpty] is false or null otherwise
     *
     * @since 0.5
     */
    fun nonEmptyOrNull(input: CharSequence): String? {
        return if (isEmpty(input)) {
            null
        } else {
            input.toString()
        }
    }

    /**
     * Check that input is email.
     *
     * @param input Char sequence which can be email
     * @return true if input is not empty and match to [.emailer] pattern
     */
    fun isEmail(input: CharSequence): Boolean {
        return !isEmpty(input) && emailer.matcher(input).matches()
    }

    /**
     * Check that input is phone
     *
     * @param input Char sequence which can be phone
     * @return true if not [.isEmpty] and match to [.phonePattern]
     *
     * @since 0.5
     */
    fun isPhone(input: CharSequence): Boolean {
        return !isEmpty(input) && phonePattern.matcher(input).matches()
    }

    /**
     * Convert date from Iso8601 format to [Date]. Example of Iso8601:
     * 2011-09-24T19:45:31+02:00
     *
     * @param iso8601 Input with date in Iso8601 format
     * @return Date from iso8601 or null if iso8601 is incorrect
     */
    fun iso8601ToDate(iso8601: String): Date? {
        if (isEmpty(iso8601)) return null
        try {
            return sdf.parse(iso8601)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Convert date from [Date] to Iso8601. Example of Iso8601:
     * 2011-09-24T19:45:31+02:00
     *
     * @param date Date to format
     * @return String of date in iso8601 format
     */
    @JvmStatic fun dateToIso8601String(date: Date): String {
        return sdf.format(date)
    }
}
