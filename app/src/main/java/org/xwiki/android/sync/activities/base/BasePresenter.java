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
package org.xwiki.android.sync.activities.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Base presenter for use MVP pattern.
 *
 * @param <T> Type of target Class for set exactly {@link MVPView} child
 *
 * @version $Id$
 */
public abstract class BasePresenter<T extends MVPView> implements Presenter<T> {

    /**
     * Android {@link Context} to know where this presenter is work.
     */
    protected Context context;
    /**
     * Current target.
     */
    private T mMvpView;

    /**
     * Base constructor to create base instance of presenter.
     *
     * @param context Context, can't be changed by default.
     */
    protected BasePresenter(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Attach mvpView to to current presenter.
     *
     * @param mvpView {@link MVPView} which will be attached to presenter.
     */
    @Override
    public void attachView(@NonNull T mvpView) {
        mMvpView = mvpView;
    }

    /**
     * Detach {@link #mMvpView} from presenter.
     */
    @Override
    public void detachView() {
        mMvpView = null;
    }

    /**
     * @return true if {@link #mMvpView} was correctly set up by {@link #attachView(MVPView)} and
     * not detached by {@link #detachView()}.
     */
    public boolean isViewAttached() {
        return mMvpView != null;
    }

    /**
     * @return {@link #mMvpView}
     */
    @Nullable
    public T getMvpView() {
        return mMvpView;
    }

    /**
     * @throws MvpViewNotAttachedException when view has no attached
     */
    public void checkViewAttached() throws MvpViewNotAttachedException {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    /**
     * Exception which must be thrown if {@link android.view.View} was not attached.
     */
    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }
}
