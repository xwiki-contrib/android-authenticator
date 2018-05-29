package org.xwiki.android.authenticator.utils;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class IntentUtils {
    public static Intent openLink(String url) {
        // if protocol isn't defined use http by default
        if (!TextUtils.isEmpty(url) && !url.contains("://")) {
            url = "http://" + url;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }
}
