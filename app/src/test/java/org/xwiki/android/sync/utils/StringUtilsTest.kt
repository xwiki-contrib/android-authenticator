package org.xwiki.android.sync.utils

import org.junit.Before
import org.junit.Test

import java.util.Date

import org.junit.Assert.*

/**
 * StringUtilsTest.
 */
class StringUtilsTest {
    @Before
    @Throws(Exception::class)
    fun setUp() {

    }

    @Test
    @Throws(Exception::class)
    fun isEmpty() {
        assertTrue(StringUtils.isEmpty(null))
        assertTrue(StringUtils.isEmpty(""))
        assertFalse(StringUtils.isEmpty("121"))
        assertTrue(StringUtils.isEmpty("\n\n"))
        assertFalse(StringUtils.isEmpty("\n12\n"))
    }

    @Test
    @Throws(Exception::class)
    fun isEmail() {
        assertTrue(StringUtils.isEmail("fitz.lee@outlook.com"))
        assertTrue(StringUtils.isEmail("fitz.lee.lee@o.com"))
        assertFalse(StringUtils.isEmail("fitz.lee@outlook"))
        assertFalse(StringUtils.isEmail("@outlook.com"))
        assertFalse(StringUtils.isEmail("fitz.lee@outlook:"))
        assertFalse(StringUtils.isEmail("fitz@@"))
        assertFalse(StringUtils.isEmail(""))
    }

    @Test
    @Throws(Exception::class)
    fun iso8601ToDate() {
        //assertNotNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31+02:00"));
        assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+0200"))
        //assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+02"));
        assertNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31"))
        assertNull(StringUtils.iso8601ToDate("2011-092419:45:31"))
        assertNull(StringUtils.iso8601ToDate("201"))
        assertNull(StringUtils.iso8601ToDate(""))
    }

    @Test
    @Throws(Exception::class)
    fun dateToIso8601String() {
        assertNotNull(StringUtils.dateToIso8601String(Date()))
        //System.out.println(StringUtils.dateToIso8601String(new Date()));
    }

    @Test
    @Throws(Exception::class)
    fun isValidServerAddress() {
        assertNotNull(StringUtils.validServerAddress("localhost:8080/xwiki"))
        assertNotNull(StringUtils.validServerAddress("https://www.xwiki.org/xwiki"))
        assertNotNull(StringUtils.validServerAddress("http://www.xwiki.org/xwiki"))
        assertNotNull(StringUtils.validServerAddress("www.xwiki.org/xwiki"))
        assertNotNull(StringUtils.validServerAddress("http://www.xwiki.org/xwiki/"))
    }

}