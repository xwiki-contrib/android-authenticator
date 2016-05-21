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

import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.utils.StringUtils;

/**
 * Created by fitz on 2016/5/16.
 */
public class SignUpStep1ViewFlipper extends BaseViewFlipper {
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEmailView;
    //private EditText mCellPhoneView;

    public SignUpStep1ViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);

        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);
        mEmailView = (EditText) findViewById(R.id.email);
        //mCellPhoneView = (EditText) findViewById(R.id.cell_phone);
    }

    @Override
    public void doNext() {
        if (checkInput()) {
            mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_UP_STEP2);
        }
    }

    @Override
    public void doPrevious() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_IP);
    }

    private boolean checkInput() {
        String email = mEmailView.getText().toString();
        if (!TextUtils.isEmpty(email) && !StringUtils.isEmail(email)) {
            mEmailView.setError(mContext.getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
            return false;
        }
        return true;
    }

    public String[] getValues() {
        String[] strings = new String[3];
        strings[0] = mFirstNameView.getText().toString();
        if (strings[0] == null) strings[0] = "";
        strings[1] = mLastNameView.getText().toString();
        if (strings[1] == null) strings[1] = "";
        strings[2] = mEmailView.getText().toString();
        //strings[3] = mCellPhoneView.getText().toString();
        //if(strings[3]==null) strings[3] = "";
        return strings;
    }
}
