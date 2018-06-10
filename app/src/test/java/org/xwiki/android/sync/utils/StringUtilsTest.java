package org.xwiki.android.sync.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * StringUtilsTest.
 */
public class StringUtilsTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void isEmpty() throws Exception {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty("121"));
        assertTrue(StringUtils.isEmpty("\n\n"));
        assertFalse(StringUtils.isEmpty("\n12\n"));
    }

    @Test
    public void isEmail() throws Exception {
        assertTrue(StringUtils.isEmail("fitz.lee@outlook.com"));
        assertTrue(StringUtils.isEmail("fitz.lee.lee@o.com"));
        assertFalse(StringUtils.isEmail("fitz.lee@outlook"));
        assertFalse(StringUtils.isEmail("@outlook.com"));
        assertFalse(StringUtils.isEmail("fitz.lee@outlook:"));
        assertFalse(StringUtils.isEmail("fitz@@"));
        assertFalse(StringUtils.isEmail(null));
        assertFalse(StringUtils.isEmail(""));
    }

    @Test
    public void getDataTime() throws Exception {
        assertNotNull(StringUtils.getDataTime("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    @Test
    public void isIpAddress() throws Exception {
        assertTrue(StringUtils.isIpAddress("192.168.0.1"));
        assertTrue(StringUtils.isIpAddress("1.1.0.255"));
        assertFalse(StringUtils.isIpAddress("192."));
        assertFalse(StringUtils.isIpAddress("192.168"));
        assertFalse(StringUtils.isIpAddress("192.168.0"));
        assertFalse(StringUtils.isIpAddress("outlook.com"));
        assertFalse(StringUtils.isIpAddress("aa"));
        assertFalse(StringUtils.isIpAddress(""));
        assertFalse(StringUtils.isIpAddress(null));
    }

    @Test
    public void isDomainAddress() throws Exception {
        assertTrue(StringUtils.isDomainAddress("xwiki.org"));
        assertTrue(StringUtils.isDomainAddress("www.xwiki.org"));
        assertTrue(StringUtils.isDomainAddress("www.abc.co.uk"));
        assertFalse(StringUtils.isDomainAddress("z"));
        assertFalse(StringUtils.isDomainAddress(".com"));
        assertFalse(StringUtils.isDomainAddress("a."));
        assertFalse(StringUtils.isDomainAddress(""));
        assertFalse(StringUtils.isDomainAddress(null));
    }

    @Test
    public void iso8601ToDate() throws Exception {
        //assertNotNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31+02:00"));
        assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+0200"));
        //assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+02"));
        assertNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31"));
        assertNull(StringUtils.iso8601ToDate("2011-092419:45:31"));
        assertNull(StringUtils.iso8601ToDate("201"));
        assertNull(StringUtils.iso8601ToDate(""));
        assertNull(StringUtils.iso8601ToDate(null));
    }

    @Test
    public void dateToIso8601String() throws Exception {
        assertNotNull(StringUtils.dateToIso8601String(new Date()));
        //System.out.println(StringUtils.dateToIso8601String(new Date()));
        assertNull(StringUtils.iso8601ToDate(null));
    }

}