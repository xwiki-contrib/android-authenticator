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

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import org.xwiki.android.sync.R
import org.xwiki.android.sync.SERVER_ADDRESS
import org.xwiki.android.sync.auth.AuthenticatorActivity
import org.xwiki.android.sync.utils.putValue

import java.net.MalformedURLException
import java.net.URL

/**
 * Flipper for setting XWiki server address.
 *
 * @version $Id: 1764429bc21ab6d1aaa331ae01d81ac5b9afd1f2 $
 */
class SettingServerIpViewFlipper
/**
 * Standard constructor.
 *
 * @param activity Actual [AuthenticatorActivity]
 * @param contentRootView Root [View] of that flipper (not activity)
 */
    (activity: AuthenticatorActivity, contentRootView: View) : BaseViewFlipper(activity, contentRootView) {

    /**
     * Check typed server address and call sign in if all is ok.
     */
    override fun doNext() {
        val serverAddress = checkInput()
        if (serverAddress != null) {
            putValue(mContext, SERVER_ADDRESS, serverAddress)
        }
    }

    /**
     * Do nothing (Setting server IP is first operation of adding account.
     */
    override fun doPrevious() {}

    /**
     * @return Valid server address or null
     */
    private fun checkInput(): String? {
        val serverEditText = findViewById<EditText>(R.id.accountServer)
        serverEditText.error = null
        val serverAddress = serverEditText.text.toString()

        if (TextUtils.isEmpty(serverAddress)) {
            serverEditText.error = mContext.getString(R.string.error_field_required)
            serverEditText.requestFocus()
            return null
        } else {
            try {
                URL(serverAddress)
                return serverAddress
            } catch (e: MalformedURLException) {
                Log.e(SettingServerIpViewFlipper::class.java.simpleName, "Wrong url", e)
                serverEditText.error = mContext.getString(R.string.error_invalid_server)
                serverEditText.requestFocus()
                return null
            }

        }
    }
}
