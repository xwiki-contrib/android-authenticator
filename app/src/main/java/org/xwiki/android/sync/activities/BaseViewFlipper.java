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

package org.xwiki.android.sync.activities;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.View;
import androidx.annotation.NonNull;
import org.xwiki.android.sync.auth.AuthenticatorActivity;

/**
 * Base class for any flipper which will be used in authenticator.
 *
 * @version $Id$
 */
public abstract class BaseViewFlipper {

    /**
     * Activity for operations.
     */
    protected AuthenticatorActivity mActivity;

    /**
     * Context of operations executing. In fact, represent {@link #mActivity}.
     */
    protected Context mContext;

    /**
     * Root view of flipper.
     */
    protected View mContentRootView;

    /**
     * All childs of this class must use only this constructor!
     *
     * @param activity Current activity
     * @param contentRootView Root view of current flipper
     */
    public BaseViewFlipper(
        @NonNull AuthenticatorActivity activity,
        @NonNull View contentRootView
    ) {
        mActivity = activity;
        mContext = mActivity;
        mContentRootView = contentRootView;
    }

    /**
     * @param id Resource identifier in view
     * @param <T> Result type
     * @return Result of calling of {@link View#findViewById(int)} as for {@link #mContentRootView}
     */
    public <T extends View> T findViewById(int id) {
        return mContentRootView.findViewById(id);
    }

    /**
     * Must be called when current flipper page must be slided to next.
     */
    public abstract void doNext();

    /**
     * Must be called when current flipper page must be slided to previous.
     */
    public abstract void doPrevious();

    /**
     * @return Title of flipper or null if have no
     */
    @Nullable
    public String getTitle() {
        return null;
    }
}
