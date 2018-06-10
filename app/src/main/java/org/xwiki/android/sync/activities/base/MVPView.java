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

public interface MVPView {

    /**
     * Should be called when a time taking process starts and we want the user
     * to wait for the process to finish. The UI should gracefully display some
     * sort of progress bar or animation so that the user knows that the app is
     * doing some work and has not stalled.
     *
     * For example: a network request to the API is made for authenticating
     * the user.
     */
    void showProgress();

    /**
     * Should be called when a time taking process ends and we have some result
     * for the user.
     */
    void hideProgress();
}
