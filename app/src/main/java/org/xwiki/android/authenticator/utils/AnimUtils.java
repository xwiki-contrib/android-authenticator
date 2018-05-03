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
