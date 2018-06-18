/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync.activities;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.auth.AuthenticatorActivity;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Flipper for setting XWiki server address.
 *
 * @version $Id$
 */
public class SettingServerIpViewFlipper extends BaseViewFlipper {

    /**
     * Standard constructor.
     *
     * @param activity Actual {@link AuthenticatorActivity}
     * @param contentRootView Root {@link View} of that flipper (not activity)
     */
    public SettingServerIpViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
    }

    /**
     * Check typed server address and call sign in if all is ok.
     */
    @Override
    public void doNext() {
        String serverAddress = checkInput();
        if (serverAddress != null) {
            SharedPrefsUtils.putValue(mContext, Constants.SERVER_ADDRESS, serverAddress);
            mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_IN);
        }
    }

    /**
     * Do nothing (Setting server IP is first operation of adding account.
     */
    @Override
    public void doPrevious() { }

    /**
     * @return Valid server address or null
     */
    @Nullable
    private String checkInput() {
        EditText serverEditText = findViewById(R.id.accountServer);
        serverEditText.setError(null);
        String serverAddress = serverEditText.getText().toString();

        if (TextUtils.isEmpty(serverAddress)) {
            serverEditText.setError(mContext.getString(R.string.error_field_required));
            serverEditText.requestFocus();
            return null;
        } else {
            try {
                new URL(serverAddress);
                return serverAddress;
            } catch (MalformedURLException e) {
                Log.e(SettingServerIpViewFlipper.class.getSimpleName(), "Wrong url", e);
                serverEditText.setError(mContext.getString(R.string.error_invalid_server));
                serverEditText.requestFocus();
                return null;
            }
        }
    }
}
