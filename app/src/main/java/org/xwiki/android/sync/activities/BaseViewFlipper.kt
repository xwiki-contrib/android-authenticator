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

package org.xwiki.android.sync.activities

import android.content.Context
import android.view.View
import org.xwiki.android.sync.auth.AuthenticatorActivity

/**
 * Base class for any flipper which will be used in authenticator.
 *
 * @version $Id: f3c3c73d439018e529af43268d6d8c807d59f06e $
 */

/**
 * All childs of this class must use only this constructor!
 *
 * @param activity Current activity
 * @param contentRootView Root view of current flipper
 */

abstract class BaseViewFlipper(protected var mActivity: AuthenticatorActivity, protected var mContentRootView: View) {

    /**
     * Context of operations executing. In fact, represent [.mActivity].
     */
    protected var mContext: Context

    init {
        mContext = mActivity
    }

    /**
     * @param id Resource identifier in view
     * @param <T> Result type
     * @return Result of calling of [View.findViewById] as for [.mContentRootView]
    </T> */
    fun <T : View> findViewById(id: Int): T {
        return mContentRootView.findViewById(id)
    }

    /**
     * Must be called when current flipper page must be slided to next.
     *
     * @return true if it is possible to go to the next step. Returns false otherwise
     */
    abstract fun doNext(): Boolean

    /**
     * Must be called when current flipper page must be slided to previous.
     */
    abstract fun doPrevious()
}
