package org.xwiki.android.authenticator.activities;

import android.content.Context;
import android.view.View;

import org.xwiki.android.authenticator.auth.AuthenticatorActivity;

/**
 * Created by lf on 2016/5/16.
 */
public abstract class BaseViewFlipper {
    protected AuthenticatorActivity mActivity;
    protected Context mContext;
    private View mContentRootView;

    public BaseViewFlipper(AuthenticatorActivity activity, View contentRootView){
        mActivity = activity;
        mContext = (Context) mActivity;
        mContentRootView = contentRootView;
    }

    public View findViewById(int id) {
        return mContentRootView.findViewById(id);
    }

    public abstract void doNext();
    public abstract void doPrevious();

}
