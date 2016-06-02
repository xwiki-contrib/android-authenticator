package org.xwiki.android.authenticator.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

import org.xwiki.android.authenticator.R;

/**
 * Created by lf on 2016/6/2.
 */
public class AnimUtils {

    /**
     * animation refresh
     */
    private static Animation animation;

    /**
     * refreshImageView
     * start the animation for image view
     * @param mContext
     * @param v
     */
    public static void refreshImageView(Context mContext, View v) {
        hideRefreshAnimation(v);
        //refresh anim
        animation = android.view.animation.AnimationUtils.loadAnimation(mContext, R.anim.refresh);
        //Defines what this animation should do when it reaches the end
        animation.setRepeatMode(Animation.RESTART);
        //repeat times
        animation.setRepeatCount(Animation.INFINITE);
        //ImageView startt anim
        v.startAnimation(animation);
    }

    /**
     * hideRefreshAnimation
     * stop the refresh animation
     * @param v
     */
    public static void hideRefreshAnimation(View v) {
        if (animation != null) {
            animation.cancel();
            v.clearAnimation();
            v.setAnimation(null);
        	//v.setImageResource(R.drawable.refresh);
        }
    }

}
