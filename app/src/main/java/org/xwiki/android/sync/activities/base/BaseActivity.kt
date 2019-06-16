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

import android.app.ProgressDialog
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.xwiki.android.sync.R

/**
 * Base class for any activity in project (must be).
 *
 * @version $Id: 86ea6146a44b9f886fdcc09bfa5c49668c5fb0ac $
 */
abstract class BaseActivity : AppCompatActivity() {

    /**
     * Currently used toolbar.
     */
    protected var toolbar: Toolbar? = null

    /**
     * Dialog which will be called and changed by [.hideProgressDialog],
     * [.showProgressDialog], [.showProgressDialog], [.getProgressDialog]
     */
    /**
     * @return [.progress]
     */
    lateinit var progressDialog: ProgressDialog

    /**
     * Work as [super.setContentView], but also prepare other components
     *
     * @param layoutResID Resource which will be used for [super.setContentView].
     */
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        toolbar = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        }
    }

    /**
     * This method is use to provide back button feature in the toolbar of activities.
     */
    protected fun showBackButton() {
        if (supportActionBar != null) {
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * Displays a toast in current activity. The duration can of two types:
     *
     *  * SHORT
     *  * LONG
     *
     *
     * @param message   Message that the toast must show.
     * @param toastType Duration for which the toast must be visible.
     */
    @JvmOverloads
    fun showToast(message: String, toastType: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@BaseActivity, message, toastType).show()
    }

    /**
     * Show progress dialog with message which was in params.
     *
     * @param message Message which will be shown with dialog
     */
    @JvmOverloads
    fun showProgressDialog(message: String = getString(R.string.pleaseWait)) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this, ProgressDialog.STYLE_SPINNER)
            progressDialog.setCancelable(true)
        }
        progressDialog.setMessage(message)
        progressDialog.show()
    }

    /**
     * Hide progress dialog if it currently visible.
     */
    fun hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    /**
     * Check selected item and if it was home button - call [.onBackPressed].
     *
     * @param item The menu item which was selected.
     * @return super# [android.app.Activity.onOptionsItemSelected]
     * @see .onCreateOptionsMenu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
/**
 * Displays a toast in current activity. In this method the duration
 * supplied is Short by default. If you want to specify duration
 * use [BaseActivity.showToast] method.
 *
 * @param message Message that the toast must show.
 */
/**
 * Show progress dialog with default message.
 */
