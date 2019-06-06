package org.xwiki.android.sync.utils;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xwiki.android.sync.AppContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.xwiki.android.sync.AppContextKt.getAppContextInstance;
import static org.xwiki.android.sync.utils.SharedPrefsUtilsKt.getArrayList;
import static org.xwiki.android.sync.utils.SharedPrefsUtilsKt.putArrayList;

/**
 * SharedPrefsUtilsTest.
 */

@RunWith(AndroidJUnit4.class)
public class SharedPrefsUtilsTest {

    Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = getAppContextInstance().getApplicationContext();
    }

    @Test
    public void arrayList() throws Exception {
        List<String> mList = new ArrayList<>();
        for(int i=0; i<10; i++){
            mList.add("i="+i);
        }
        putArrayList(mContext, "mList", mList);
        List<String> mList2 = getArrayList(mContext, "mList");
        assertEquals(mList.size(), mList2.size());
        for(String item : mList) {
            assertTrue(mList2.contains(item));
        }
        //two arrays are not equal because the set sequence is different.
        //assertArrayEquals(mList.toArray(), mList2.toArray());
    }
}
