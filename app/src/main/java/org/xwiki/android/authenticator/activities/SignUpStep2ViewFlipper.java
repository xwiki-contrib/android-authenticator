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
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.utils.AnimUtils;
import org.xwiki.android.authenticator.utils.SharedPrefsUtils;

import java.io.IOException;

/**
 * SignUpStep2ViewFlipper.
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

    private AsyncTask mSignUpTask = null;

    public SignUpStep2ViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
        initView();
        initData();
    }

    void initView(){
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
    }

    public void initData() {
        AnimUtils.refreshImageView(mContext, mActivity.refreshImageView);
        //init form token and cookie
        AsyncTask initFormTokenTask = new AsyncTask<Void, Void, HttpResponse>() {
            @Override
            protected HttpResponse doInBackground(Void... params) {
                try {
                    HttpResponse response = XWikiHttp.signUpInitCookieForm();
                    return response;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(HttpResponse response) {
                if(response == null){
                    //network error
                    AnimUtils.hideRefreshAnimation(mActivity.refreshImageView);
                    showErrorMessage("network error, check network and pull to refresh please.");
                }else{
                    int statusCode = response.getResponseCode();
                    if (statusCode < 200 || statusCode > 299) {
                        AnimUtils.hideRefreshAnimation(mActivity.refreshImageView);
                        //server or client error
                        showErrorMessage("init form error: "+response.getResponseMessage()+", pull to refresh please");
                    }else{
                        //200ok
                        SharedPrefsUtils.putValue(AppContext.getInstance().getApplicationContext(), Constants.COOKIE, response.getHeaders().get("Set-Cookie"));
                        byte[] contentData = response.getContentData();
                        Document document = Jsoup.parse(new String(contentData));
                        //get the form token
                        formToken = document.select("input[name=form_token]").val();
                        //to get captcha
                        refreshCaptcha();
                    }
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                AnimUtils.hideRefreshAnimation(mActivity.refreshImageView);
            }
        };
        mActivity.putAsyncTask(initFormTokenTask);
    }

    @Override
    public void doNext() {
        //sign up and next setting sync
        if (checkInput()) {
            mActivity.showProgress(mContext.getText(R.string.sign_up_authenticating), mSignUpTask);
            register();
        }
    }

    @Override
    public void doPrevious() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_UP_STEP1);
    }


    //0:false, 1:true, null:network error, 2:the user exists.
    public void register() {
        final String[] step1Values = mActivity.getStep1Values();
        mSignUpTask = new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... params) {
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
                if (userFind != null) return userFind;

                //sign up
                try {
                    //boolean signUpFlag = XWikiHttp.signUp(userId, password, formToken, captcha);
                    HttpResponse response = XWikiHttp.signUp(userId, password, formToken, captcha, step1Values[0], step1Values[1], step1Values[2]);
                    return response;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Object object) {
                mActivity.hideProgress();
                if(object == null){
                    showErrorMessage("network error");
                } else if(object instanceof XWikiUser){
                    //the user exists
                    showErrorMessage("the user exists");
                }else if(object instanceof HttpResponse){
                    HttpResponse response = (HttpResponse) object;
                    int statusCode = response.getResponseCode();
                    //response error
                    if (statusCode < 200 || statusCode > 299) {
                        showErrorMessage(response.getResponseMessage());
                        refreshCaptcha();
                        return;
                    }
                    //return 200ok
                    byte[] contentData = response.getContentData();
                    Document document = Jsoup.parse(new String(contentData));
                    Elements elements = document.select("#loginForm");
                    if(!elements.isEmpty()){
                        //200ok, sign up successfully because html contains "id=loginForm"
                        finishSignUp();
                        mActivity.hideInputMethod();
                        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_SYNC);
                    }else{
                        //return 200ok! but not sign up successfully
                        showErrorMessage("Captcha error");
                        refreshCaptcha();
                    }
                }
            }
        };
        mActivity.putAsyncTask(mSignUpTask);

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
            return true;
        }
    }


    void refreshCaptcha() {
        AsyncTask refreshCaptchaTask = new AsyncTask<Void, Void, byte[]>() {
            @Override
            protected byte[] doInBackground(Void... params) {
                //String captchaUrl = "http://210.76.192.253:8080/xwiki/bin/imagecaptcha/XWiki/Registration";
                //String url = "http://www.xwiki.org/xwiki/bin/imagecaptcha/XWiki/RealRegistration";
                String captchaUrl = XWikiHttp.getServerAddress() + "/bin/imagecaptcha/XWiki/Registration";
                if (captchaUrl.contains("www.xwiki.org")) {
                    captchaUrl = XWikiHttp.getServerAddress() + "/bin/imagecaptcha/XWiki/RealRegistration";
                }
                byte[] img = null;
                try {
                    img = XWikiHttp.downloadImage(captchaUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return img;
            }

            @Override
            protected void onPostExecute(byte[] bytes) {
                AnimUtils.hideRefreshAnimation(mActivity.refreshImageView);
                Bitmap captchaBitmap = getPicFromBytes(bytes, null);
                mCaptchaImageView.setImageBitmap(captchaBitmap);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                AnimUtils.hideRefreshAnimation(mActivity.refreshImageView);
            }
        };
        mActivity.putAsyncTask(refreshCaptchaTask);
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


    private void showErrorMessage(String error){
        //Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
        final TextView errorTextView = (TextView) findViewById(R.id.error_msg);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(error);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                errorTextView.setVisibility(View.GONE);
            }
        }, 2000);
    }

}
