package org.xwiki.android.sync.utils

import android.content.Context
import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.AppContext

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.xwiki.android.sync.appContext
import org.xwiki.android.sync.utils.getArrayList
import org.xwiki.android.sync.utils.putArrayList

/**
 * SharedPrefsUtilsTest.
 */

@RunWith(AndroidJUnit4::class)
class SharedPrefsUtilsTest {

    private lateinit var mContext: Context

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mContext = appContext.applicationContext
    }

    @Test
    @Throws(Exception::class)
    fun arrayList() {
        val mList = ArrayList<String>()
        for (i in 0..9) {
            mList.add("i=$i")
        }
        putArrayList(mContext, "mList", mList)
        val mList2 = getArrayList(mContext, "mList")
        assertEquals(mList.size.toLong(), mList2!!.size.toLong())
        for (item in mList) {
            assertTrue(mList2.contains(item))
        }
        //two arrays are not equal because the set sequence is different.
        //assertArrayEquals(mList.toArray(), mList2.toArray());
    }
}
