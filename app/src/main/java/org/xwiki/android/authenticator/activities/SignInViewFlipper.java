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
public class SignInViewFlipper extends BaseViewFlipper{

    public SignInViewFlipper(AuthenticatorActivity activity, View contentRootView) {
        super(activity, contentRootView);
    }

    @Override
    public void doNext() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_SYNC);
    }

    @Override
    public void doPrevious() {
        mActivity.showViewFlipper(AuthenticatorActivity.ViewFlipperLayoutId.SETTING_IP);
    }
}
