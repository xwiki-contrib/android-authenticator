package org.xwiki.android.authenticator.activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.rest.XWikiHttp;

/**
 * Created by lf on 2016/5/16.
 */
public class SignUpStep2ViewFlipper extends BaseViewFlipper{

    public SignUpStep2ViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
    }

    @Override
    public void doNext() {
        //sign up and next setting sync
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_SYNC);
    }

    @Override
    public void doPrevious() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SIGN_UP_STEP1);
    }

    void finishSignUp() {
        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, "fitz");
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        //data.putString(AccountManager.KEY_AUTHTOKEN, "tokentoken");
        data.putString(AuthenticatorActivity.PARAM_USER_SERVER, XWikiHttp.getServerRequestUrl());
        data.putString(AuthenticatorActivity.PARAM_USER_PASS, "leee");
        Intent intent = new Intent();
        intent.putExtras(data);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }
}
