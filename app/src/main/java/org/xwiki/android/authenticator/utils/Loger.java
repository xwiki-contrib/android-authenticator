package org.xwiki.android.authenticator.utils;

import android.util.Log;

/**
 * Log Manager
 */
public final class Loger {
    public static boolean IS_DEBUG = true;

    public static final void debug(String msg) {
        if (IS_DEBUG) {
            Log.i("debug", msg);
        }
    }

    public static final void debug(String msg, Throwable tr) {
        if (IS_DEBUG) {
            Log.i("debug", msg, tr);
        }
    }

    public static final void exception(Exception e) {
        if (IS_DEBUG) {
            e.printStackTrace();
        }
    }

    public static final void debug(String msg, Object... format) {
        debug(String.format(msg, format));
    }
}
