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
import org.xwiki.android.authenticator.utils.StringUtils;

/**
 * Created by fitz on 2016/5/16.
 */
public class SettingIpViewFlipper extends BaseViewFlipper {

    CharSequence serverAddr = null;
    CharSequence serverPort = null;

    public SettingIpViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
    }

    @Override
    public void doNext() {
        if (checkInput()) {
            SharedPrefsUtils.putValue(mContext, Constants.SERVER_ADDRESS, serverAddr.toString());
            mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_UP_STEP1);
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
        EditText serverPortEditText = (EditText) findViewById(R.id.accountPort);
        serverEditText.setError(null);
        serverPortEditText.setError(null);
        serverAddr = serverEditText.getText();
        serverPort = serverPortEditText.getText();
        View focusView = null;
        boolean cancel = false;
        if (!StringUtils.isIpAddress(serverAddr) && !StringUtils.isDomainAddress(serverAddr)) {
            focusView = serverEditText;
            serverEditText.setError(mContext.getString(R.string.error_invalid_server));
            cancel = true;
        } else if (TextUtils.isEmpty(serverPort)) {
            focusView = serverEditText;
            serverEditText.setError(mContext.getString(R.string.error_invalid_server));
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            if (StringUtils.isIpAddress(serverAddr)) {
                serverAddr = serverAddr + ":" + serverPort;
            }
            return true;
        }
    }

}
