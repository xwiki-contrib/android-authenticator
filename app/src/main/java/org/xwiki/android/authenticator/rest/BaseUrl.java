package org.xwiki.android.authenticator.rest;

import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

/**
 * Created by Pawan Pal on 11/03/17.
 */

public class BaseUrl {

    static String getBaseUrl() {
        return SharedPrefsUtils.getValue(AppContext.getInstance().getApplicationContext(),
                Constants.SERVER_ADDRESS, null) + "/";
    }
}
