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

import androidx.annotation.NonNull;

/**
 * Presenter interface for use MVP pattern.
 *
 * @param <V> Type of target Class for set exactly {@link MVPView} child.
 *
 * @version $Id$
 */
public interface Presenter<V extends MVPView> {

    /**
     * Attach mvpView to to current presenter.
     *
     * @param mvpView {@link MVPView} which will be attached to presenter.
     */
    void attachView(@NonNull V mvpView);

    /**
     * Detach {@link android.view.View} from presenter.
     */
    void detachView();

}
