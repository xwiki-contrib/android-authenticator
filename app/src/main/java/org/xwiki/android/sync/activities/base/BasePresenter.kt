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
package org.xwiki.android.sync.activities.base

import android.content.Context

/**
 * Base presenter for use MVP pattern.
 *
 * @param <T> Type of target Class for set exactly [MVPView] child
 *
 * @version $Id: 9bdc211c36cebc43d658be9dd02276308e8a5ac9 $
</T> */
abstract class BasePresenter<T : MVPView>
/**
 * Base constructor to create base instance of presenter.
 *
 * @param context Context, can't be changed by default.
 */
protected constructor(
    /**
     * Android [Context] to know where this presenter is work.
     */
    protected var context: Context
) : Presenter<T> {
    /**
     * Current target.
     */
    /**
     * @return [.mMvpView]
     */
    var mvpView: T? = null
        private set

    /**
     * @return true if [.mMvpView] was correctly set up by [.attachView] and
     * not detached by [.detachView].
     */
    val isViewAttached: Boolean
        get() = mvpView != null

    /**
     * Attach mvpView to to current presenter.
     *
     * @param mvpView [MVPView] which will be attached to presenter.
     */
    override fun attachView(mvpView: T) {
        this.mvpView = mvpView
    }

    /**
     * Detach [.mMvpView] from presenter.
     */
    override fun detachView() {
        mvpView = null
    }

    /**
     * @throws MvpViewNotAttachedException when view has no attached
     */
    @Throws(BasePresenter.MvpViewNotAttachedException::class)
    fun checkViewAttached() {
        if (!isViewAttached) throw MvpViewNotAttachedException()
    }

    /**
     * Exception which must be thrown if [android.view.View] was not attached.
     */
    class MvpViewNotAttachedException :
        RuntimeException("Please call Presenter.attachView(MvpView) before" + " requesting data to the Presenter")
}
