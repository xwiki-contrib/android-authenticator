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

import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.rest.XWikiHttp;

import java.io.IOException;

/**
 * Created by fitz on 2016/5/16.
 */
public class SignUpStep2ViewFlipper extends BaseViewFlipper {
    private EditText mUserIdEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private EditText mCaptchaEditText;
    private ImageView mCaptchaImageView;

    String formToken = null;
    String userId = null;
    String password = null;
    String confirmPassword = null;
    String captcha = null;

    public SignUpStep2ViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
        initData();
    }

    void initData() {
        // init view
        mUserIdEditText = (EditText) findViewById(R.id.user_id_edit);
        mPasswordEditText = (EditText) findViewById(R.id.password);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.confirm_password);
        mCaptchaEditText = (EditText) findViewById(R.id.captcha_edit);
        mCaptchaImageView = (ImageView) findViewById(R.id.captcha_image);
        mCaptchaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });

        //init form token and cookie
        new AsyncTask<String, String, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    formToken = XWikiHttp.signUpInitCookieForm();
                    return formToken == null ? false : true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean flag) {
                if (flag == null) {
                    Toast.makeText(mContext, "init form network error", Toast.LENGTH_SHORT).show();
                } else if (!flag) {
                    Toast.makeText(mContext, "init form error", Toast.LENGTH_SHORT).show();
                } else {
                    refreshCaptcha();
                }
            }
        }.execute();
    }

    @Override
    public void doNext() {
        //sign up and next setting sync
        if (checkInput()) {
            register();
            mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_SYNC);
        }
    }

    @Override
    public void doPrevious() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_UP_STEP1);
    }


    //0:false, 1:true, null:network error, 2:the user exists.
    public void register() {
        final String[] step1Values = mActivity.getStep1Values();
        new AsyncTask<String, String, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                //found whether the user exists.
                XWikiUser userFind = null;
                try {
                    userFind = XWikiHttp.getUserDetail("xwiki", "XWiki", userId);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    return null;
                }
                if (userFind != null) return 2;

                try {
                    //boolean signUpFlag = XWikiHttp.signUp(userId, password, formToken, captcha);
                    boolean signUpFlag = XWikiHttp.signUp(userId, password, formToken, captcha, step1Values[0], step1Values[1], step1Values[2]);
                    return signUpFlag ? 1 : 0;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Integer status) {
                if (status == null) {
                    Toast.makeText(mContext, "network error", Toast.LENGTH_SHORT).show();
                } else if (status == 2) {
                    Toast.makeText(mContext, "the user exists", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, status == 1 ? "success!" : "fail!", Toast.LENGTH_SHORT).show();
                    if (status == 0) {
                        refreshCaptcha();
                    } else {
                        finishSignUp();
                    }
                }
            }
        }.execute();
    }

    void finishSignUp() {
        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, userId);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        data.putString(AuthenticatorActivity.PARAM_USER_SERVER, XWikiHttp.getServerAddress());
        data.putString(AuthenticatorActivity.PARAM_USER_PASS, password);
        Intent intent = new Intent();
        intent.putExtras(data);
        mActivity.finishLogin(intent);
    }

    /**
     * Attempts to check input
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no server checking is made.
     */
    private boolean checkInput() {
        // Reset errors.
        mUserIdEditText.setError(null);
        mPasswordEditText.setError(null);
        mConfirmPasswordEditText.setError(null);
        mCaptchaEditText.setError(null);

        // Store values at the time of the login attempt.
        userId = mUserIdEditText.getText().toString();
        password = mPasswordEditText.getText().toString();
        confirmPassword = mConfirmPasswordEditText.getText().toString();
        captcha = mCaptchaEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(userId)) {
            mUserIdEditText.setError(mContext.getString(R.string.error_field_required));
            focusView = mUserIdEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(password) || password.length() < 6) {
            mPasswordEditText.setError(mContext.getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(confirmPassword) || !confirmPassword.equals(password)) {
            mConfirmPasswordEditText.setError(mContext.getString(R.string.error_confirm_password));
            focusView = mConfirmPasswordEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(captcha)) {
            mCaptchaEditText.setError(mContext.getString(R.string.error_invalid_captcha));
            focusView = mCaptchaEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            //TODO Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            return true;
        }
    }


    void refreshCaptcha() {
        new AsyncTask<String, String, byte[]>() {
            @Override
            protected byte[] doInBackground(String... params) {
                //String captchaUrl = "http://210.76.192.253:8080/xwiki/bin/imagecaptcha/XWiki/Registration";
                //String url = "http://www.xwiki.org/xwiki/bin/imagecaptcha/XWiki/RealRegistration";
                String captchaUrl = "http://" + XWikiHttp.getServerAddress() + "/xwiki/bin/imagecaptcha/XWiki/Registration";
                if (captchaUrl.contains("www.xwiki.org")) {
                    captchaUrl = "http://" + XWikiHttp.getServerAddress() + "/xwiki/bin/imagecaptcha/XWiki/RealRegistration";
                }
                byte[] img = XWikiHttp.downloadAvatar(captchaUrl);
                return img;
            }

            @Override
            protected void onPostExecute(byte[] bytes) {
                Bitmap captchaBitmap = getPicFromBytes(bytes, null);
                mCaptchaImageView.setImageBitmap(captchaBitmap);
            }
        }.execute();
    }

    public static Bitmap getPicFromBytes(byte[] bytes,
                                         BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

}
