package org.xwiki.android.sync.utils

import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

/**
 * StringUtilAndroidTest.
 */
@RunWith(AndroidJUnit4::class)
class StringUtilAndroidTest {

    @Before
    @Throws(Exception::class)
    fun setUp() {

    }

    @Test
    @Throws(Exception::class)
    fun iso8601ToDate() {
        // this test in android runtime is different from that in java runtime
        // for iso8601, Z or X ? Maybe java is standard.
        assertNotNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31+02:00"))
        assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+0200"))
        assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+02"))
        assertNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31"))
        assertNull(StringUtils.iso8601ToDate("2011-092419:45:31"))
        assertNull(StringUtils.iso8601ToDate("201"))
        assertNull(StringUtils.iso8601ToDate(""))
    }

}
