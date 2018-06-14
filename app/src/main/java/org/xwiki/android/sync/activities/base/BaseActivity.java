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

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.xwiki.android.sync.R;

/**
 * Base class for any activity in project (must be).
 *
 * @version $Id$
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Currently used toolbar.
     */
    protected Toolbar toolbar;

    /**
     * Dialog which will be called and changed by {@link #hideProgressDialog()},
     * {@link #showProgressDialog()}, {@link #showProgressDialog(String)}, {@link #getProgressDialog()}
     */
    private ProgressDialog progress;

    /**
     * Work as {@link super#setContentView(int)}, but also prepare other components
     *
     * @param layoutResID Resource which will be used for {@link super#setContentView(int)}.
     */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    /**
     * This method is use to provide back button feature in the toolbar of activities.
     */
    protected void showBackButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Displays a toast in current activity. In this method the duration
     * supplied is Short by default. If you want to specify duration
     * use {@link BaseActivity#showToast(String, int)} method.
     *
     * @param message Message that the toast must show.
     */
    public void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    /**
     * Displays a toast in current activity. The duration can of two types:
     * <ul>
     * <li>SHORT</li>
     * <li>LONG</li>
     * </ul>
     *
     * @param message   Message that the toast must show.
     * @param toastType Duration for which the toast must be visible.
     */
    public void showToast(@NonNull String message, int toastType) {
        Toast.makeText(BaseActivity.this, message, toastType).show();
    }

    /**
     * Show progress dialog with default message.
     */
    public void showProgressDialog() {
        showProgressDialog(getString(R.string.pleaseWait));
    }

    /**
     * Show progress dialog with message which was in params.
     *
     * @param message Message which will be shown with dialog
     */
    public void showProgressDialog(String message) {
        if (progress == null) {
            progress = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(true);
        }
        progress.setMessage(message);
        progress.show();
    }

    /**
     * Hide progress dialog if it currently visible.
     */
    public void hideProgressDialog() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    /**
     * @return {@link #progress}
     */
    public ProgressDialog getProgressDialog() {
        return progress;
    }

    /**
     * Check selected item and if it was home button - call {@link #onBackPressed()}.
     *
     * @param item The menu item which was selected.
     * @return super# {@link android.app.Activity#onOptionsItemSelected(MenuItem)}
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
