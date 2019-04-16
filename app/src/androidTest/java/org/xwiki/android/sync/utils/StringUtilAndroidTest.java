package org.xwiki.android.sync.utils;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * StringUtilAndroidTest.
 */
@RunWith(AndroidJUnit4.class)
public class StringUtilAndroidTest {

    @Before
    public void setUp() throws Exception{

    }

    @Test
    public void iso8601ToDate() throws Exception {
        // this test in android runtime is different from that in java runtime
        // for iso8601, Z or X ? Maybe java is standard.
        assertNotNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31+02:00"));
        assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+0200"));
        assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+02"));
        assertNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31"));
        assertNull(StringUtils.iso8601ToDate("2011-092419:45:31"));
        assertNull(StringUtils.iso8601ToDate("201"));
        assertNull(StringUtils.iso8601ToDate(""));
        assertNull(StringUtils.iso8601ToDate(null));
    }

}
