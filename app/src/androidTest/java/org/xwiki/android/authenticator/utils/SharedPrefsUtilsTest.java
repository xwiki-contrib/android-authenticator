package org.xwiki.android.authenticator.utils;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xwiki.android.authenticator.AppContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SharedPrefsUtilsTest.
 */

@RunWith(AndroidJUnit4.class)
public class SharedPrefsUtilsTest {

    Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = AppContext.getInstance().getApplicationContext();
    }

    @Test
    public void arrayList() throws Exception {
        List<String> mList = new ArrayList<>();
        for(int i=0; i<10; i++){
            mList.add("i="+i);
        }
        SharedPrefsUtils.putArrayList(mContext, "mList", mList);
        List<String> mList2 = SharedPrefsUtils.getArrayList(mContext, "mList");
        assertEquals(mList.size(), mList2.size());
        for(String item : mList) {
            assertTrue(mList2.contains(item));
        }
        //two arrays are not equal because the set sequence is different.
        //assertArrayEquals(mList.toArray(), mList2.toArray());
    }
}
