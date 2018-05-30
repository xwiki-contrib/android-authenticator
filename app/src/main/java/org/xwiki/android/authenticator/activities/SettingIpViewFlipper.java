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
package org.xwiki.android.authenticator.activities;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * SettingIpViewFlipper
 */
public class SettingIpViewFlipper extends BaseViewFlipper {

    CharSequence serverAddr = null;

    public SettingIpViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
    }

    @Override
    public void doNext() {
        if (checkInput()) {
            SharedPrefsUtils.putValue(mContext, Constants.SERVER_ADDRESS, serverAddr.toString());
            mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_IN);
        }
    }

    @Override
    public void doPrevious() {
        if (checkInput()) {
            SharedPrefsUtils.putValue(mContext, Constants.SERVER_ADDRESS, serverAddr.toString());
            mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_IN);
        }
    }

    public boolean checkInput() {
        EditText serverEditText = (EditText) findViewById(R.id.accountServer);
        serverEditText.setError(null);
        serverAddr = serverEditText.getText();
        View focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(serverAddr)) {
            focusView = serverEditText;
            serverEditText.setError(mContext.getString(R.string.error_field_required));
            cancel = true;
        }else{
            try {
                URL url = new URL(serverAddr.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                focusView = serverEditText;
                serverEditText.setError(mContext.getString(R.string.error_invalid_server));
                cancel = true;
            }
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

}
