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
package org.xwiki.android.sync.activities.editcontact;

import org.xwiki.android.sync.activities.base.MVPView;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Interface which represent EditContact as {@link MVPView}. Implementers must provide work with
 * view as with edit contact view - in fact, contains only basic methods such as callback for
 * auth and other.
 *
 * @version $Id$
 */
public interface EditContactMvpView extends MVPView {

    /**
     * Show to user that contact was successfully updated.
     */
    void showContactUpdateSuccessfully();

    /**
     * Show to user that contact was not updated for the some reason.
     */
    void showErrorOnUpdatingContact();

    /**
     * Actually update cookie and restart update contact.
     *
     * @param response Body which must contains new cookies
     */
    void showLoginSuccessfully(Response<ResponseBody> response);

    /**
     * Show to user that authorisation was failed.
     */
    void showErrorLogin();

    /**
     * Call this method for init progress dialog cancel listener.
     */
    void initOnProgressDialogCancel();
}
